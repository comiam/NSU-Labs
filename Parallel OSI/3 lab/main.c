#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <syscall.h>

typedef struct arg
{
    char* argc;
    pthread_t thread_to_wait;
} t_args;

void* routine(void *args)
{
    t_args* arg = (t_args*)args;

    if(arg->thread_to_wait)
        pthread_join(arg->thread_to_wait, NULL);

    printf("%s from thread %lu\n", arg->argc, syscall(__NR_gettid));

    return 0;
}

t_args args[4];

int main(int argc, char* argv[])
{
    pthread_t last;

    for (int i = 0; i < 5; ++i)
    {
        t_args new = {argv[i], i ? last : 0};
        args[i] = new;

        pthread_create(&last, NULL, routine, &(args[i]));
    }

    pthread_exit(0);
}

