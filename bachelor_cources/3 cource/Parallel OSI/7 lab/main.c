#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#define num_steps 200000000

typedef struct arg
{
    int index;
    int totalCount;
    double pi;
} t_args;

void* routine(void *args)
{
    t_args* arg = (t_args*)args;

    int index = arg->index;
    double pi = 0;

    int step = num_steps / arg->totalCount;

    for (int i = step * index; i < step * (index + 1); ++i)
    {
        pi += 1.0 / (i * 4.0 + 1.0);
        pi -= 1.0 / (i * 4.0 + 3.0);
    }

    t_args* arg0 = (t_args*)malloc(sizeof(t_args));
    arg0->pi = pi;

    pthread_exit(arg0);
}

int main(int argc, char** argv)
{
    int thread_count;
    if(argc != 2)
    {
        printf("Invalid arg size!");
        return 0;
    }else
    {
        char *endptr;
        thread_count = strtol(argv[1], &endptr, 10);
        if (*endptr != '\0')
        {
            printf("`%s' are not numbers\n", argv[1]);
            return 0;
        }
    }

    pthread_t threads[thread_count];
    pthread_t last;
    t_args args[thread_count];

    for (int i = 0; i < thread_count; ++i)
    {
        t_args new = {i, thread_count, 0.0};
        args[i] = new;

        int code = pthread_create(&last, NULL, routine, &(args[i]));

        if(code)
        {
            printf("Thread %i created with error: %i\n", i, code);

            for (int j = 0; j < i; ++j)
                pthread_cancel(threads[j]);
            return 0;
        }
        else
            printf("Thread %i created successfully\n", i);

        threads[i] = last;
    }

    double pi = 0;

    for (int i = 0; i < thread_count; ++i)
        {
            t_args *arg;
            int code = pthread_join(threads[i], (void**)&arg);

            if(code)
                printf("Thread %i completed with error: %i\n", i, code);
            else
                printf("Thread %i completed successfully\n", i);
            pi += arg->pi;
            free(arg);
        }

    printf("pi done - %.15g \n", 4*pi);

    return (EXIT_SUCCESS);
}