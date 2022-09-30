#ifndef MPI_THREADS_LIST_H
#define MPI_THREADS_LIST_H

#include <ctime>
#include <cstring>
#include <climits>
#include <algorithm>
#include <vector>
#include <random>

#define MAX_COMPUTATIONAL_COMPLEXITY 20

#define I_KNOW_COMPUTATIONAL_COMPLEXITY 1
#define I_DONT_KNOW_COMPUTATIONAL_COMPLEXITY 0

#define TOTAL_LIST_SIZE 20
#define EMPTY_LISTS     0
#define SUCCESS_BALANCE 1

int* initJobList(int rank);
bool nonEmpty(const int* list);
int  popLast(int* list);
int  listSize(const int* list);
int  rebalance(int** lists, int size, int mode);

#endif
