#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void ex(void *args)
{
    sprintf(stdout, "----Im died----");
    fflush(stdout);
}

void *routine(void *args)
{
    pthread_cleanup_push(ex, NULL);

    while (1)
        printf("I'M ALIVE!!!\n");
    //h
    pthread_cleanup_pop(1);
    //g
    return NULL;
}

int main(int argc, char *argv[])
{
    pthread_t last;
    int code = pthread_create(&last, NULL, routine, NULL);

    if(code)
    {
        printf("lel error");
        return 0;
    }

    sleep(2);

    code = pthread_cancel(last);
    if(code)
    {
        printf("lel error cancel");
        return 0;
    }
    sleep(1);
    return 0;
}
