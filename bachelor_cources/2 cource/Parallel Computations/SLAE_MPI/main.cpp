#include <cstdio>
#include <cmath>
#include <cstdlib>
#include <mpi.h>

void calcMatrixParts(int *vecSize, int *vecStartPos, int *matrixSize, int *matrixBeginPos, int processCount);

void loadData(double **A, double **b);

const int N = 2500;
const double epsilon = 1e-5;
double tau = 0.015;

int main(int argc, char **argv)
{
    int processCount;
    int processRank;
    double start = 0;
    double partNorm = 0;
    double sumNorm = 0;
    double normB = 0;

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &processCount);
    MPI_Comm_rank(MPI_COMM_WORLD, &processRank);

    int *vecSize = static_cast<int*>(malloc(sizeof(int) * processCount));
    int *vecStartPos = static_cast<int*>(malloc(sizeof(int) * processCount));
    int *matrixBeginPos = static_cast<int*>(malloc(sizeof(int) * processCount));
    int *matrixSize = static_cast<int*>(malloc(sizeof(int) * processCount));

    auto *A = static_cast<double *>(malloc(sizeof(double) * N * N));
    auto *b = static_cast<double *>(malloc(sizeof(double) * N));
    auto *x = static_cast<double *>(malloc(sizeof(double) * N));

    if (processRank == 0)
        loadData(&A, &b);

    calcMatrixParts(vecSize, vecStartPos, matrixSize, matrixBeginPos, processCount);

    printf("I'm %d from %d processes and my lines: %d-%d (%d lines)\n", processRank, processCount, matrixBeginPos[processRank] / N,
           (matrixBeginPos[processRank] + matrixSize[processRank]) / N, matrixSize[processRank] / N);

    auto *buf0 = static_cast<double *>(malloc(sizeof(double) * vecSize[processRank]));
    auto *buf1 = static_cast<double *>(malloc(sizeof(double) * N));

    auto *partA = static_cast<double *>(malloc(sizeof(double) * vecSize[processRank] * N));
    auto *partB = static_cast<double *>(malloc(sizeof(double) * vecSize[processRank]));

    MPI_Scatterv(A, matrixSize, matrixBeginPos, MPI_DOUBLE, partA, matrixSize[processRank], MPI_DOUBLE, 0,MPI_COMM_WORLD);
    MPI_Scatterv(b, vecSize, vecStartPos, MPI_DOUBLE, partB, vecSize[processRank], MPI_DOUBLE, 0, MPI_COMM_WORLD);

    if (processRank == 0)
    {
        start = MPI_Wtime();

        for (int i = 0; i < N; ++i)
            normB += b[i] * b[i];

        normB = sqrt(normB);
    }

    bool flag = true;
    bool diverge0 = false;
    bool diverge1 = false;
    bool setOld = false;
    double oldValue = 0;
    int divergeCount = 0;
    int normCount = 0;

    while (flag)
    {
        //Ax - b
        for (int i = 0; i < vecSize[processRank]; i++)
        {
            double sum = 0;
            for (int j = 0; j < N; j++)
                sum += partA[i * N + j] * x[j];

            buf0[i] = sum - partB[i];
            partNorm += buf0[i] * buf0[i];//calc norm |Ax-b|
        }

        MPI_Reduce(&partNorm, &sumNorm, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
        MPI_Allgatherv(buf0, vecSize[processRank], MPI_DOUBLE, buf1, vecSize, vecStartPos, MPI_DOUBLE,
                       MPI_COMM_WORLD);

        //x - tau(Ax-b)
        for (int i = 0; i < N; i++)
            x[i] = x[i] - tau * buf1[i];

        if (processRank == 0)
        {
            sumNorm = sqrt(sumNorm);
            flag = sumNorm / normB > epsilon;

            if (!setOld)
                setOld = true;
            else if (oldValue < sumNorm / normB)
                divergeCount++;
            else
                normCount++;

            if(normCount > 30)
            {
                divergeCount = 0;
                normCount = 0;
            }

            //std::cout << sumNorm / normB << std::endl;

            oldValue = sumNorm / normB;

            if (divergeCount > 50)
            {
                if (diverge0)
                {
                    diverge1 = true;
                    flag = false;
                } else
                {
                    diverge0 = true;
                    tau *= -1;
                    divergeCount = 0;
                    normCount = 0;
                }
            }
        }

        MPI_Bcast(&tau, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
        MPI_Bcast(&flag, 1, MPI_INT, 0, MPI_COMM_WORLD);
        partNorm = 0;
    }

    if (processRank == 0)
    {
        double end = MPI_Wtime();
        if(diverge1)
            std::cout << "We haven't solution!" << std::endl;
        else
        {
            FILE *ifsAns = fopen("vecX.bin", "r");
            float a = 0;

            for (int i = 0; i < N; ++i)
            {
                fread(((void *) &a), sizeof(float), 1, ifsAns);
                std::cout << a << " = " << x[i] << std::endl;
            }
        }

        std::cout << "Processes: " << processCount << ", time: " << (end - start) << std::endl;
    }


    delete[](vecSize);
    delete[](matrixSize);
    delete[](vecStartPos);
    delete[](matrixBeginPos);
    delete[](x);
    delete[](buf0);
    delete[](partA);
    delete[](partB);
    if (processRank == 0)
    {
        delete[](A);
        delete[](b);
    }
    MPI_Finalize();
    return 0;
}

void loadData(double **A, double **b)
{
    FILE *A_INP = fopen("matA.bin", "rb");
    FILE *B_INP = fopen("vecB.bin", "rb");

    fread((*A), sizeof(double), N * N, A_INP);
    fread((*b), sizeof(double), N, B_INP);

    fclose(A_INP);
    fclose(B_INP);
}

void calcMatrixParts(int *vecSize, int *vecStartPos, int *matrixSize, int *matrixBeginPos, int processCount)
{
    int curOffset = 0;
    for (int i = 0; i < processCount; i++)
        vecSize[i] = N / processCount;

    for (int i = 0; i < processCount; i++)
    {
        if (i < N % processCount) vecSize[i]++;
        vecStartPos[i] = curOffset;
        curOffset += vecSize[i];
        matrixBeginPos[i] = vecStartPos[i] * N;
        matrixSize[i] = vecSize[i] * N;
    }
}