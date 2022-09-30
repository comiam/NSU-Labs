#include "messages.h"

void recvMsgFlag(int &flag, int src)
{
    MPI_Recv(&flag, 1, MPI_INT, src, MPI_LOL_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
}

void recvMsgArray(int* array, int size, int src)
{
    MPI_Recv(array, size, MPI_INT, src, MPI_KEK_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
}

void sendMsgFlag(int &flag, int dest)
{
    MPI_Send(&flag, 1, MPI_INT, dest, MPI_LOL_TAG, MPI_COMM_WORLD);
}

void sendMsgArray(int* array, int size, int dest)
{
    MPI_Send(array, size, MPI_INT, dest, MPI_KEK_TAG, MPI_COMM_WORLD);
}