#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <signal.h>

typedef struct arg
{
    int index;
    int totalCount;
    double pi;
} t_args;

#define num_steps 10000
int returnFromThreads = 0;
pthread_barrier_t ownb;
t_args *args;
pthread_t *threads;

void* routine(void *args)
{
    t_args* arg = (t_args*)args;

    int index = arg->index;
    double pi = 0;

    int condBarrier = arg->totalCount > 1;

    for (int j = 0;!returnFromThreads; ++j)
    {
        for (int i = num_steps * (index + j); i < num_steps * (index + j + 1); ++i)
        {
            pi += 1.0 / (i * 4.0 + 1.0);
            pi -= 1.0 / (i * 4.0 + 3.0);
        }
        if(condBarrier)
            pthread_barrier_wait(&(ownb));
    }

    t_args* arg0 = (t_args*)malloc(sizeof(t_args));
    arg0->pi = pi;

    pthread_exit(arg0);
}

void sigStop(int signum)
{
    returnFromThreads = 1;
}

void set_signal_handler(int sig, void (*handler)(int))
{
    struct sigaction act;

    act.sa_handler = handler;
    act.sa_flags = 0;

    /* Recommended for SIGCHLD signal. Read POSIX. */
    if(sig == SIGCHLD)
        act.sa_flags |= SA_RESTART;

    sigemptyset(&act.sa_mask);
    sigaction(sig, &act, ((void *)0));
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

    args = (t_args*)malloc((thread_count + 1) * sizeof(t_args));
    threads = (pthread_t*)malloc((thread_count + 1) * sizeof(pthread_t));
    args[thread_count].index = -1;
    threads[thread_count] = 0;

    pthread_t last;

    if(pthread_barrier_init(&ownb, NULL, thread_count))
    {
        perror("pthread_barrier_init");
        return -1;
    }

    set_signal_handler(SIGINT, sigStop);
    set_signal_handler(SIGTERM, sigStop);

    for(int i = 0; i < thread_count; ++i)
    {
        t_args new = {.index = i, .totalCount = thread_count, .pi = 0.0};
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
    pthread_barrier_destroy(&ownb);

    return (EXIT_SUCCESS);
}