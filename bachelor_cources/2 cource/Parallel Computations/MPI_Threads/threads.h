#ifndef MPI_THREADS_THREADS_H
#define MPI_THREADS_THREADS_H
#include <thread>
#include <mutex>
#include <condition_variable>
#include <cstdarg>
#include "messages.h"
#include "list.h"

#define TASK_REQUEST_STEP_2  4
#define TASK_REQUEST_STEP_1  3
#define NEED_TASKS           2
#define CONTINUE_WORK        1
#define STOP_WORK            0
#define EMPTY  -1

void lockList();
void unlockList();
bool isDead();

void worker      (int* list_ref);
void communicator(int* list_ref, int iKnowCompFlexibility);
void threadSavePrint(const std::string& message, FILE *file, ...);

#endif
