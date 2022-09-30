#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>

pthread_mutex_t m1;
pthread_cond_t condVar;
int mode = 1;

typedef struct arg
{
    int first;
} t_args;

void destroyMutex()
{
    pthread_mutex_destroy(&m1);
    pthread_cond_destroy(&condVar);
}

void stop(char *errorMsg)
{
    destroyMutex();
    perror(errorMsg);
    exit(EXIT_FAILURE);
}

void initAll()
{
    if (pthread_mutex_init(&m1, NULL))
    {
        perror("Error creating mutex");
        exit(EXIT_FAILURE);
    }

    if(pthread_cond_init(&condVar,NULL))
    {
        pthread_mutex_destroy(&m1);
        perror("Error creating condition variable");
        exit(EXIT_FAILURE);
    }
}

void *print(void* args)
{
    t_args* arg = (t_args*)args;
    int myMode = arg->first;

    if(pthread_mutex_lock(&m1))
        stop("Error locking mutex");

    for (int i = 0; i < 10; ++i)
    {
        while(mode != myMode)
            pthread_cond_wait(&condVar,&m1);

        printf(myMode ? "first %i\n" : "second %i\n", i);
        mode = myMode ? 0 : 1;

        pthread_cond_signal(&condVar);
    }
    if(pthread_mutex_unlock(&m1))
        stop("Error unlocking mutex");

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
    
    destroyMutex();
    return EXIT_SUCCESS;
}