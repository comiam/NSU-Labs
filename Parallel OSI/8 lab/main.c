#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <signal.h>

#define num_steps 200000000

typedef struct arg
{
    int index;
    int totalCount;
    double pi;
    pthread_barrier_t ownb;
} t_args;

_Noreturn void* routine(void *args)
{
    t_args* arg = (t_args*)args;

    t_args* arg0 = (t_args*)malloc(sizeof(t_args));
    arg0->index = arg->index;
    arg0->pi = 0;

    int step = num_steps / arg->totalCount;

    for (int j = 0;; ++j)
        for (int i = step * arg0->index + num_steps * j; i < step * (arg0->index + 1) + num_steps * j; ++i)
        {
            arg0->pi += 1.0 / (i * 4.0 + 1.0);
            arg0->pi -= 1.0 / (i * 4.0 + 3.0);

            if(i && i % 100 == 0 && arg->totalCount > 1)
                pthread_barrier_wait(&(arg->ownb));
        }
}

t_args *args;
pthread_t *threads;

/* Set signal handler for special signal. */
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

void sigint(int signum)
{
    double pi = 0;

    int i = 0;

    printf("lol");
    while (threads[i] != 0)
        pthread_cancel(threads[i]);

    while (args[i].index != -1)
        pi += args[i++].pi;

    free(args);
    free(threads);

    printf("lol");

    printf("I got pi = %f", pi);
    fflush(stdout);
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

    pthread_barrier_t barrier;
    pthread_t last;

    pthread_barrier_init(&barrier, NULL, thread_count);

    sigset_t set;
    sigemptyset(&set);

    sigaddset(&set, SIGINT);
    pthread_sigmask(SIG_)

    for (int i = 0; i < thread_count; ++i)
    {
        t_args new = {.index = i, .totalCount = thread_count, .pi = 0.0, .ownb = barrier};
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
        }

    printf("pi done - %.15g \n", 4*pi);

    return (EXIT_SUCCESS);
}