#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>
#include <semaphore.h>

sem_t sem0;
sem_t sem1;

typedef struct arg
{
    int first;
} t_args;

void destroySemaphores()
{
    sem_destroy(&sem0);
    sem_destroy(&sem1);
}

void stop(char *errorMsg)
{
    destroySemaphores();
    perror(errorMsg);
    exit(EXIT_FAILURE);
}

void initAll()
{
    if (sem_init(&sem0, 0, 1) || sem_init(&sem1, 0, 0))
    {
        perror("Error creating mutex");
        exit(EXIT_FAILURE);
    }
}

void *print(void* args)
{
    t_args* arg = (t_args*)args;
    int myMode = arg->first;

    for (int i = 0; i < 10; ++i)
    {
        sem_wait(myMode ? &sem0 : &sem1);
        printf(myMode ? "first %i\n" : "second %i\n", i);
        sem_post(!myMode ? &sem0 : &sem1);
    }

    return NULL;
}

int main()
{
    pthread_t threadId;

    initAll();

    t_args arg0 = {0};
    t_args arg1 = {1};

    if (pthread_create(&threadId, NULL, print, &(arg0)))
        stop("Error creating thread");
    
    print(&(arg1));

    if (pthread_join(threadId, NULL))
        stop("Error waiting thread");

    destroySemaphores();
    return EXIT_SUCCESS;
}