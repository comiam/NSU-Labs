#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>

pthread_mutexattr_t attr;
pthread_mutex_t m1;
pthread_mutex_t m2;
pthread_mutex_t m3;

void destroyMutex()
{
    pthread_mutex_destroy(&m1);
    pthread_mutex_destroy(&m2);
    pthread_mutex_destroy(&m3);
}

void stop(char *errorMsg)
{
    destroyMutex();
    perror(errorMsg);
    exit(EXIT_FAILURE);
}

void lockMutex(pthread_mutex_t *m)
{
    if (pthread_mutex_lock(m))
        stop("Error lock mutex");
}

void unlockMutex(pthread_mutex_t *m)
{
    if (pthread_mutex_unlock(m))
        stop("Error unlock mutex");
}

void initMutexes()
{
    pthread_mutexattr_init(&attr);
    if (pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_ERRORCHECK))
    {
        perror("Error creating attributes\n");
        exit(EXIT_FAILURE);
    }

    if (pthread_mutex_init(&m1, &attr) ||
        pthread_mutex_init(&m2, &attr) ||
        pthread_mutex_init(&m3, &attr))
    {
        destroyMutex();
        perror("Error creating mutex");
        exit(EXIT_FAILURE);
    }
}

void *second()
{
    lockMutex(&m2);
    for (int i = 0; i < 10; ++i)
    {
        lockMutex(&m1);

        printf("kek %i\n", i);

        unlockMutex(&m2);
        lockMutex(&m3);
        unlockMutex(&m1);
        lockMutex(&m2);
        unlockMutex(&m3);
    }
    unlockMutex(&m2);
    return NULL;
}

void first()
{
    for (int i = 0; i < 10; ++i)
    {
        printf("kuk %i\n", i);

        lockMutex(&m3);
        unlockMutex(&m1);
        lockMutex(&m2);
        unlockMutex(&m3);
        lockMutex(&m1);
        unlockMutex(&m2);
    }
    unlockMutex(&m1);
}

int main()
{
    pthread_t threadId;

    initMutexes();
    lockMutex(&m1);

    if (pthread_create(&threadId, NULL, second, NULL))
        stop("Error creating thread");

    if(sleep(1))
        stop("Sleep was interrupted");
    
    first();

    if (pthread_join(threadId, NULL))
        stop("Error waiting thread");
    
    destroyMutex();
    return EXIT_SUCCESS;
}