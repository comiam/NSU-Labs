#include <stdio.h>
#include <unistd.h>
#include <semaphore.h>
#include <pthread.h>

sem_t sem[4];

_Noreturn void *createA(void *arg)
{
    while (1)
    {
        sleep(1);
        sem_post(&sem[0]);
        printf("A created!\n");
    }
}

_Noreturn void *createB(void *arg)
{
    while (1)
    {
        sleep(2);
        sem_post(&sem[1]);
        printf("B created!\n");
    }
}

_Noreturn void *createC(void *arg)
{
    while (1)
    {
        sleep(3);
        sem_post(&sem[2]);
        printf("C created!\n");
    }
}

_Noreturn void *createModule(void *arg)
{
    while (1)
    {
        sem_wait(&sem[0]);
        sem_wait(&sem[1]);
        sem_post(&sem[3]);
        printf("module created!\n");
    }
}

_Noreturn void *createWidget()
{
    while (1)
    {
        sem_wait(&sem[3]);
        sem_wait(&sem[2]);
        printf("Widget created!\n");
    }
}

int main()
{
    pthread_t aThread;
    pthread_t bThread;
    pthread_t cThread;
    pthread_t moduleThread;

    for (int i = 0; i < 4; ++i)
        sem_init(&sem[i], 0, 0);

    pthread_create(&aThread, NULL, createA, NULL);
    pthread_create(&bThread, NULL, createB, NULL);
    pthread_create(&cThread, NULL, createC, NULL);
    pthread_create(&moduleThread, NULL, createModule, NULL);
    createWidget();
}