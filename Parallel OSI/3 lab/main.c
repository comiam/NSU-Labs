#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <syscall.h>

typedef struct arg
{
    char** argc;
    int size;
} t_args;

void* routine(void *args)
{
    t_args* arg = (t_args*)args;

    for (int i = 1; i < arg->size; ++i)
        printf("%s --- %lu\n", arg->argc[i], syscall(__NR_gettid));

    return 0;
}

int main(int argc, char* argv[])
{
    t_args args[argc - 1];
    pthread_t threads[argc - 1];
    pthread_t last;

    for (int i = 1; i < argc; ++i)
    {
        t_args new = {argv, argc};
        args[i - 1] = new;

        int code = pthread_create(&last, NULL, routine, &(args[i - 1]));

        if(code)
        {
            printf("Thread %i created with error: %i\n", i - 1, code);
            threads[i - 1] = 0;
            continue;
        }
        else
            printf("Thread %i created successfully\n", i);

        threads[i - 1] = last;
    }

    for (int i = 0; i < argc - 1; ++i)
        if(threads[i] != 0)
        {
            int code = pthread_join(threads[i], NULL);
            if(code)
                printf("Thread %i completed with error: %i\n", i, code);
            else
                printf("Thread %i completed successfully\n", i);
        }

    return 0;
}

