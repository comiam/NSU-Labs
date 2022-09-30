#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <semaphore.h>
#include <wait.h>

sem_t *sem0;
sem_t *sem1;

void unlinkSemaphore(char *semName)
{
    if (sem_unlink(semName))
    {
        perror("Error unlinking semaphore");
        exit(EXIT_FAILURE);
    }
}

void destroySemaphore(sem_t *sem)
{
    if (sem_close(sem))
    {
        perror("Error destroying semaphore");
        exit(EXIT_FAILURE);
    }
}


void stop(char *errorMsg)
{
    destroySemaphore(sem0);
    destroySemaphore(sem1);
    unlinkSemaphore("sem0");
    unlinkSemaphore("sem1");
    perror(errorMsg);
    exit(EXIT_FAILURE);
}

void initAll()
{
    sem_t *_sem = sem_open("sem0", O_CREAT, 0777, 1);

    if (_sem == SEM_FAILED)
    {
        perror("Error creating semaphore");
        exit(EXIT_FAILURE);
    } else
        sem0 = _sem;

    _sem = sem_open("sem1", O_CREAT, 0777, 0);

    if (_sem == SEM_FAILED)
    {
        perror("Error creating semaphore");
        destroySemaphore(sem0);
        unlinkSemaphore("sem0");
        exit(EXIT_FAILURE);
    } else
        sem1 = _sem;
}

void print(int myMode)
{
    for (int i = 0; i < 10; ++i)
    {
        sem_wait(myMode ? sem0 : sem1);
        printf(myMode ? "first %i\n" : "second %i\n", i);
        sem_post(!myMode ? sem0 : sem1);
    }
}

int main()
{
    initAll();
    pid_t pid = fork();

    if (pid < 0)
        stop("Error creating process");

    if (pid == 0)
        print(1);
    else
        print(0);

    if (pid)
    {
        if (wait(NULL) < 0)
            stop("Error waiting child");

        unlinkSemaphore("sem0");
        unlinkSemaphore("sem1");
    }

    destroySemaphore(sem0);
    destroySemaphore(sem1);
    return EXIT_SUCCESS;
}