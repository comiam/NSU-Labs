#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void ex(void *args)
{
    printf("----Im died----");
    fflush(stdout);
}

void *routine(void *args)
{
    pthread_cleanup_push(ex, NULL)

    while (1)
        printf("I'M ALIVE!!!\n");

    pthread_cleanup_pop(1);
    return NULL;
}

int main(int argc, char *argv[])
{
    pthread_t last;
    pthread_create(&last, NULL, routine, NULL);

    sleep(2);

    pthread_cancel(last);
    sleep(1);
    return 0;
}
