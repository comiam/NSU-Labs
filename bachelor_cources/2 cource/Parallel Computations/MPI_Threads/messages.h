#ifndef MPI_THREADS_MESSAGES_H
#define MPI_THREADS_MESSAGES_H

#include <mpi.h>

#define MPI_LOL_TAG 42
#define MPI_KEK_TAG 41

void recvMsgFlag(int &flag, int src);
void recvMsgArray(int* array, int size, int src);

void sendMsgFlag(int &flag, int dest);
void sendMsgArray(int* array, int size, int dest);

#endif //MPI_THREADS_MESSAGES_H
