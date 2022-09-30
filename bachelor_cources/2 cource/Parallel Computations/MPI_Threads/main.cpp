#include "threads.h"

using namespace std;

static int random(int begin, int end)
{
    return rand() % (end - begin + 1) + begin;
}

int main(int argc, char* argv[])
{
    int provided = 0;
    int rank = 0;

    MPI_Init_thread(&argc, &argv, MPI_THREAD_MULTIPLE, &provided);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if(argc != 2)
    {
        if(!rank)
            printf("Invalid arg count!\n");

        MPI_Finalize();

        return EXIT_FAILURE;
    }

    int mode = -1000;

    if(sscanf(argv[1], "%d", &mode) != 1 || (mode != I_KNOW_COMPUTATIONAL_COMPLEXITY && mode != I_DONT_KNOW_COMPUTATIONAL_COMPLEXITY))
    {
        if(!rank)
            printf("Invalid arg value!\n");

        MPI_Finalize();

        return EXIT_FAILURE;
    }

    if(provided != MPI_THREAD_MULTIPLE)
    {
        fprintf(stderr, "Some error with MPI: failed to get the required requirements!\n");
        exit(EXIT_FAILURE);
    }

    int totalGlobal = 0;

    int* list = initJobList(rank);

    for (int l = 0; l < listSize(list); ++l)
        totalGlobal += list[l];

    if(!list)
    {
        threadSavePrint("Can't alloc memory in process %d! Shutting down.", stdout, rank);
        MPI_Finalize();

        return EXIT_SUCCESS;
    }

    string message = "%*d process begin with: ";

    for (int i = 0; i < TOTAL_LIST_SIZE; ++i)
    {
        if(list[i] == -1)
            break;

        message += to_string(list[i]);
        message += " ";
    }
    message.pop_back();
    message += '\n';

    threadSavePrint(message, stdout, 2, rank);
    std::this_thread::sleep_for(std::chrono::milliseconds(500));

    MPI_Barrier(MPI_COMM_WORLD);

    double start = MPI_Wtime();

    thread worker_thread(worker, list);
    thread communicator_thread(communicator, list, mode);

    int newTask;
    bool brFlag = false;

    for (int j = 1; j <= 4; ++j)//Add task times
    {
        for (int k = 0; k < 50 * j; ++k) // after 25 sec add new task, but every second check thread is alive
        {
            std::this_thread::sleep_for(std::chrono::seconds (1));
            if(isDead())
            {
                brFlag = true;
                break;
            }
        }
        if(brFlag)
            break;

        lockList();

        threadSavePrint("======Add new tasks to node %*d\n", stdout, 2, rank);
        if(TOTAL_LIST_SIZE - listSize(list) > 1)
            for (int i = 0; i < random((TOTAL_LIST_SIZE - listSize(list)) / 2, TOTAL_LIST_SIZE - listSize(list)); ++i)
            {
                newTask = random(1, MAX_COMPUTATIONAL_COMPLEXITY);

                totalGlobal += newTask;
                list[listSize(list)] = newTask;
            }

        message = "======New list of %*d:\n";

        for (int i = 0; i < TOTAL_LIST_SIZE; ++i)
        {
            if(list[i] == -1)
                break;

            message += to_string(list[i]);
            message += " ";
        }
        message.pop_back();
        message += '\n';

        threadSavePrint(message, stdout, 2, rank);

        unlockList();
    }

    communicator_thread.join();
    worker_thread.join();

    double finish = MPI_Wtime();

    threadSavePrint("-- %*d process: shutting down\n", stdout, 2, rank);

    MPI_Barrier(MPI_COMM_WORLD);

    int commSize, sendArray[1] = {totalGlobal};

    MPI_Comm_size(MPI_COMM_WORLD, &commSize);
    int *totalArr = rank ? nullptr : (int *)malloc(commSize * sizeof(int));

    MPI_Gather(sendArray, 1, MPI_INT, totalArr, 1, MPI_INT, 0, MPI_COMM_WORLD);

    if(!rank)
    {
        int max = INT_MIN;

        for (int i = 0; i < commSize; ++i)
            if(max < totalArr[i])
                max = totalArr[i];

        threadSavePrint("Time elapsed: %d, max time: %d\n", stdout, (int)(finish - start), max);
        free(totalArr);
    }

    free(list);
    MPI_Finalize();

    return EXIT_SUCCESS;
}