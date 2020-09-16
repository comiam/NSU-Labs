#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void* routine(void *args)
{
    while(1)
        printf("I'M ALIVE!!!\n");

    return NULL;
}

int main(int argc, char* argv[])
{
    pthread_t last;
    pthread_create(&last, NULL, routine, NULL);

    sleep(2);

    pthread_cancel(last);
    printf("lol");
    return 0;
}

