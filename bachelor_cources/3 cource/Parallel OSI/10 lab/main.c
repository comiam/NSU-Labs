#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>

pthread_mutexattr_t attr;
pthread_mutex_t m[3];

void destroyMutex(int index)
{
    for (int i = 0; i < index; ++i)
        pthread_mutex_destroy(&m[i]);
}

void stop(char *errorMsg)
{
    destroyMutex(3);
    perror(errorMsg);
    exit(EXIT_FAILURE);
}

void lockMutex(int index)
{
    if (pthread_mutex_lock(&m[index]))
        stop("Error lock mutex");
}

void unlockMutex(int index)
{
    if (pthread_mutex_unlock(&m[index]))
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

    for (int i = 0; i < 3; ++i)
    {
        if(pthread_mutex_init(&m[i], &attr))
        {
            destroyMutex(i);
            perror("Error creating mutex");
            exit(EXIT_FAILURE);
        }
    }
}

void *second()
{
    int n = 0;
    lockMutex(1);
    for (int i = 0; i < 10; ++i)
    {
        lockMutex(n);
        printf("kek %i\n", i);
        unlockMutex((n+1)%3);
        n = (n+2)%3;
    }
    unlockMutex(0);
    return NULL;
}

void first()
{
    int n = 0;
    for (int i = 0; i < 10; ++i)
    {
        printf("kuk %i\n", i);

        unlockMutex(n);
        lockMutex((n+1)%3);
        n = (n+2)%3;
    }
    unlockMutex(1);
    unlockMutex(2);
}

int main()
{
    pthread_t threadId;

    initMutexes();
    lockMutex(0);
    lockMutex(2);

    if (pthread_create(&threadId, NULL, second, NULL))
        stop("Error creating thread");

    if(sleep(1))
        stop("Sleep was interrupted");
    
    first();

    if (pthread_join(threadId, NULL))
        stop("Error waiting thread");
    
    destroyMutex(3);
    return EXIT_SUCCESS;
}