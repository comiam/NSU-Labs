#include "threads.h"

std::mutex              work_mutex;
std::condition_variable work_wait;
bool                    work_notified;

int                     work_flag = EMPTY;
std::mutex              list_mutex;
std::mutex              print_mutex;

bool                    is_dead = false;

bool isDead()
{
    return is_dead;
}

static void unlockWorker(int message);

void lockList()
{
    list_mutex.lock();
}

void unlockList()
{
    list_mutex.unlock();
}

void worker(int* list_ref)
{
    int work;
    int rank;
    int flag = NEED_TASKS;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    while(true)
    {
        if(!nonEmpty(list_ref) || work_flag == STOP_WORK)
        {
            if(work_flag == STOP_WORK)
                break;

            {
                sendMsgFlag(flag, rank);

                std::unique_lock<std::mutex> locker(work_mutex);
                while(!work_notified)// may be spurious wakeup
                    work_wait.wait(locker);
            }

            if(work_flag == STOP_WORK)
                break;

            work_flag = EMPTY;
        }

        lockList();
        work = popLast(list_ref);
        unlockList();

        if(work != -1)
        {
            threadSavePrint("-- %*d process: begin do new task %*d: %*d tasks left\n", stdout, 2, rank, 2, work, 2, listSize(list_ref));
            std::this_thread::sleep_for(std::chrono::seconds(work));
            work_notified = false;
        } else if(work_notified)
            break;
    }
    is_dead = true;
}

void communicator(int* list_ref, int iKnowCompFlexibility)
{
    int rank;
    int size;
    int flag = 0;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    while (true)
    {
        recvMsgFlag(flag, MPI_ANY_SOURCE);

        if(flag == CONTINUE_WORK)
            continue;
        else if(flag == STOP_WORK)
        {
            unlockWorker(STOP_WORK);
            return;
        }
        else if(flag == TASK_REQUEST_STEP_2)
        {
            lockList();

            sendMsgArray(list_ref, TOTAL_LIST_SIZE, 0);
            recvMsgFlag(flag, 0);

            if(flag == STOP_WORK)
            {
                for (int i = 0; i < TOTAL_LIST_SIZE; ++i) //Clear list, because of its empty on rebalance
                    list_ref[i] = -1;

                unlockWorker(STOP_WORK);
                unlockList();
                return;
            }
            else
            {
                recvMsgArray(list_ref, TOTAL_LIST_SIZE, 0);
                unlockWorker(CONTINUE_WORK);
            }

            unlockList();
        }

        if(rank == 0)
        {
            switch (flag)
            {
                case NEED_TASKS:
                case TASK_REQUEST_STEP_1:
                {
                    threadSavePrint("begin rebalance\n", stdout);
                    flag = TASK_REQUEST_STEP_2;
                    for (int i = 1; i < size; ++i)
                        sendMsgFlag(flag, i);

                    bool mallocErrorFlag = false;
                    int** lists = (int**) malloc(sizeof(int*) * size);

                    if(!lists)
                        mallocErrorFlag = true;
                    else
                        for (int i = 1; i < size; ++i)
                            if(!(lists[i] = (int*) malloc(sizeof(int) * TOTAL_LIST_SIZE)))
                            {
                                for (int j = 1; j < size; ++j)
                                    if(lists[i])
                                        free(lists[i]);

                                free(lists);

                                mallocErrorFlag = true;
                                break;
                            }

                    if(mallocErrorFlag)
                    {
                        flag = STOP_WORK;
                        for (int i = 1; i < size; ++i)
                        {
                            recvMsgArray(list_ref, TOTAL_LIST_SIZE, i);
                            sendMsgFlag(flag, i);
                        }

                        threadSavePrint("Can't alloc memory in process 0! \nShutting down.\n", stderr);
                        unlockWorker(STOP_WORK);
                        return;
                    } else
                        for (int k = 1; k < size; ++k)
                            recvMsgArray(lists[k], TOTAL_LIST_SIZE, k);

                    lockList();
                    lists[0] = list_ref;

                    int res = rebalance(lists, size, iKnowCompFlexibility);
                    unlockList();

                    if(res == EMPTY_LISTS)
                    {
                        threadSavePrint("end rebalance: empty all\nclosing cluster...\n", stdout);

                        flag = STOP_WORK;
                        for (int i = 1; i < size; ++i)
                        {
                            sendMsgFlag(flag, i);
                            free(lists[i]);
                        }

                        unlockWorker(STOP_WORK);
                        free(lists);

                        return;
                    }else
                    {
                        print_mutex.lock();

                        int balanced = 0;

                        for (int j = 0; j < size; ++j)
                            for (int k = 0; k < TOTAL_LIST_SIZE; ++k)
                                if(lists[j][k] != -1)
                                    balanced += lists[j][k];
                                else
                                    break;

                        balanced /= size;
                        int weight = 0;

                        printf("rebalanced task lists:");

                        for (int l = 0; l < size; ++l)
                        {
                            printf("\n%*d - ", 3, l);
                            if(listSize(lists[l]) == 0)
                                printf("empty");
                            else
                                for (int i = 0; i < TOTAL_LIST_SIZE; ++i)
                                    if(lists[l][i] == -1)
                                    {
                                        printf("\n---- lw vs bl: %*d %*d", 2, weight, 2, balanced);
                                        weight = 0;
                                        break;
                                    }
                                    else
                                    {
                                        weight += lists[l][i];
                                        printf("%d ", lists[l][i]);
                                    }
                        }
                        printf("\n");

                        print_mutex.unlock();

                        int stoppedCount = 0;
                        for (int i = 1; i < size; ++i)
                        {
                            if(listSize(lists[i]) != 0)
                            {
                                flag = CONTINUE_WORK;
                                sendMsgFlag(flag, i);
                                sendMsgArray(lists[i], TOTAL_LIST_SIZE, i);
                            } else
                            {
                                stoppedCount++;
                                flag = STOP_WORK;
                                sendMsgFlag(flag, i);
                            }

                            free(lists[i]);
                        }
                        size -= stoppedCount;

                        unlockWorker(CONTINUE_WORK);
                        free(lists);
                    }
                    threadSavePrint("end rebalance\n", stdout);
                    break;
                }
                default:
                    threadSavePrint("Invalid receive flag value!", stderr);
                    unlockWorker(STOP_WORK);
                    return;
            }
        } else if(flag == NEED_TASKS)
            sendMsgFlag((flag = TASK_REQUEST_STEP_1), 0);
    }
}

void threadSavePrint(const std::string& message, FILE *file, ...)
{
    va_list list;

    std::lock_guard<std::mutex> locker(print_mutex);

    va_start(list, file);
    vfprintf(file, message.c_str(), list);
    fflush(file);
    va_end(list);
}

void unlockWorker(int message)
{
    std::unique_lock<std::mutex> locker(work_mutex);
    work_flag = message;
    work_notified = true;
    work_wait.notify_all();
}