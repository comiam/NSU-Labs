#include "list.h"

int popLast(int* list)
{
    if(!nonEmpty(list))
        return -1;

    for (int i = 0; i < TOTAL_LIST_SIZE; ++i)
        if(list[i] == -1)
        {
            int buf = list[i - 1];
            list[i - 1] = -1;
            return buf;
        }

    return -1;
}

static std::vector<int> sortedVec(int** lists, int size, int& total)
{
    std::vector<int> res;
    for (int i = 0; i < size; ++i)
        for (int j = 0; j < TOTAL_LIST_SIZE; ++j)
        {
            if(lists[i][j] == -1)
                break;

            res.emplace_back(lists[i][j]);
            total += lists[i][j];
        }
        
    std::sort(res.begin(), res.end());
    return res;
}

int rebalance(int** lists, int size, int mode)
{
    int total = 0;
    std::vector<int> vec = sortedVec(lists, size, total);
    if(vec.empty())
        return EMPTY_LISTS;

    for (int k = 0; k < size; ++k)
        for (int l = 0; l < TOTAL_LIST_SIZE; ++l)
            lists[k][l] = -1;

    if(mode == I_KNOW_COMPUTATIONAL_COMPLEXITY)
    {
        int balance = total / size;
        int localTotal = 0;
        int j = 0;
        int minimalDifference;
        int minDivIndex;

        for (int i = 0; i < size; ++i, localTotal = 0, j = 0)
            while (localTotal < balance && !vec.empty())
            {
                minimalDifference = INT_MAX;
                minDivIndex = 0;
                for (int k = 0; k < vec.size(); ++k)
                    if(minimalDifference > abs(balance - localTotal - vec[k]))
                    {
                        minimalDifference = abs(balance - localTotal - vec[k]);
                        minDivIndex = k;
                    }

                lists[i][j++] = vec[minDivIndex];
                localTotal += vec[minDivIndex];

                auto it = vec.begin();
                std::advance(it, minDivIndex);
                vec.erase(it);
            }

        while (!vec.empty())
        {
            int minTotal = INT_MAX;
            total = 0;
            for (int i = 0; i < size; ++i)
            {
                for (int k = 0; k < TOTAL_LIST_SIZE; ++k)
                    if(lists[i][k] != -1)
                        total += lists[i][k];
                    else
                        break;

                if(minTotal > total && listSize(lists[i]) != TOTAL_LIST_SIZE)
                {
                    j = i;
                    minTotal = total;
                }

                total = 0;
            }

            auto max = std::max_element(vec.begin(), vec.end());
            lists[j][listSize(lists[j])] = *max;

            vec.erase(max);
        }


    } else
    {
        int index = 0;

        while (!vec.empty())
        {
            lists[index][listSize(lists[index])] = vec.back();
            vec.pop_back();

            index = (index + 1) % size;
        }
    }
    return SUCCESS_BALANCE;
}

bool nonEmpty(const int* list)
{
    return list[0] != -1;
}

int listSize(const int* list)
{
    for (int i = 0; i < TOTAL_LIST_SIZE; ++i)
        if(list[i] == -1)
            return i;

    return TOTAL_LIST_SIZE;
}

static int random(int begin, int end)
{
    return rand() % (end - begin + 1) + begin;
}

int* initJobList(int rank)
{
    int* list = (int*) malloc(sizeof(int) * TOTAL_LIST_SIZE);

    if(!list)
        return nullptr;

    for (int j = 0; j < TOTAL_LIST_SIZE; ++j)
        list[j] = -1;

    srand(time(nullptr) * rank);

    int limit;
    if(rank)
        limit = random(1, TOTAL_LIST_SIZE / 2);
    else
        limit = random(TOTAL_LIST_SIZE / 2, TOTAL_LIST_SIZE);


    for (int i = 0; i < limit; ++i)
        list[i] = random(1, MAX_COMPUTATIONAL_COMPLEXITY);

    return list;
}

