#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void* routine(void *args)
{
    while(1)
    {
        pthread_testcancel();
        printf("I'M ALIVE!!!\n");
    }

    return NULL;
}

int main(int argc, char* argv[])
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
    printf("lol");
    return 0;
}

