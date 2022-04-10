#include <stdio.h>
#include <unistd.h>
#include <semaphore.h>
#include <pthread.h>
#define __USE_XOPEN_EXTENDED
#include <signal.h>

sem_t sem[4];
int returnFromThreads = 0;

void *createA(void *arg)
{
    while (!returnFromThreads)
    {
        sleep(1);
        sem_post(&sem[0]);
        printf("A created!\n");
    }
    return NULL;
}

void *createB(void *arg)
{
    while (!returnFromThreads)
    {
        sleep(2);
        sem_post(&sem[1]);
        printf("B created!\n");
    }
    return NULL;
}

void *createC(void *arg)
{
    while (!returnFromThreads)
    {
        sleep(3);
        sem_post(&sem[2]);
        printf("C created!\n");
    }
    return NULL;
}

void *createModule(void *arg)
{
    while (!returnFromThreads)
    {
        sem_wait(&sem[0]);
        sem_wait(&sem[1]);
        sem_post(&sem[3]);
        printf("module created!\n");
    }
    return NULL;
}

void *createWidget()
{
    while (!returnFromThreads)
    {
        sem_wait(&sem[3]);
        sem_wait(&sem[2]);
        printf("Widget created!\n");
    }
    return NULL;
}

void sigStop(int signum)
{
    returnFromThreads = 1;
}

int main()
{
    pthread_t aThread;
    pthread_t bThread;
    pthread_t cThread;
    pthread_t moduleThread;

    sigset(SIGINT, sigStop);
    sigset(SIGTERM, sigStop);

    for (int i = 0; i < 4; ++i)
        sem_init(&sem[i], 0, 0);

    printf("==========STARTED==========\n");
    fflush(stdout);

    pthread_create(&aThread, NULL, createA, NULL);
    pthread_create(&bThread, NULL, createB, NULL);
    pthread_create(&cThread, NULL, createC, NULL);
    pthread_create(&moduleThread, NULL, createModule, NULL);
    createWidget();

    pthread_join(aThread, NULL);
    pthread_join(bThread, NULL);
    pthread_join(cThread, NULL);
    pthread_join(moduleThread, NULL);

    for (int i = 0; i < 4; ++i)
        sem_destroy(&sem[i]);

    printf("==========CLOSED==========\n");
    fflush(stdout);


    return 0;
}