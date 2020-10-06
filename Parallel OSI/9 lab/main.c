#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

#define PHILO 5
#define DELAY 30000
#define FOOD 500

pthread_mutex_t food_mutex;
pthread_mutex_t forks[PHILO];
pthread_mutex_t cond_mutex;

pthread_cond_t getting_forks_cond;
pthread_t phils[PHILO];

int eating_now = 0;

void *philosopher(void *num);
int food_on_table();
void get_forks(int, int);
void down_forks(int, int);

int main()
{
    int i;

    pthread_mutex_init(&food_mutex, NULL);
    pthread_mutex_init(&cond_mutex, NULL);

    for (i = 0; i < PHILO; i++)
        pthread_mutex_init(&forks[i], NULL);

    pthread_cond_init(&getting_forks_cond, NULL);

    for (i = 0; i < PHILO; i++)
        pthread_create(&phils[i], NULL, philosopher, (void *) i);
    for (i = 0; i < PHILO; i++)
        pthread_join(phils[i], NULL);
    return 0;
}

void *philosopher(void *num)
{
    int id;
    int left_fork, right_fork, f;
    int total_eatings = 0;

    id = (int) num;
    printf("Philosopher %d sitting down to dinner.\n", id);
    right_fork = id;
    left_fork = id + 1;

    /* Wrap around the forks. */
    if (left_fork == PHILO)
        left_fork = 0;

    while (f = food_on_table())
    {
        printf("Philosopher %d: get dish %d.\n", id, f);
        get_forks(right_fork, left_fork);

        printf("Philosopher %d: eating.\n", id);
        usleep(DELAY * (FOOD - f + 1));
        down_forks(left_fork, right_fork);
        total_eatings++;
    }
    printf("Philosopher %d is done eating. Total: %d\n", id, total_eatings);
    return (NULL);
}

int food_on_table()
{
    static int food = FOOD;
    int myfood;

    pthread_mutex_lock(&food_mutex);
    if (food > 0)
        food--;

    myfood = food;
    pthread_mutex_unlock(&food_mutex);
    return myfood;
}

void get_forks(int fork1, int fork2)
{
    int res;


    pthread_mutex_lock(&cond_mutex);
    do
    {
        res = pthread_mutex_trylock(&forks[fork1]);
        if (res)
        {
            res = pthread_mutex_trylock(&forks[fork2]);
            if (res)
                pthread_mutex_unlock(&forks[fork1]);
        }
        while(eating_now > 1)
            pthread_cond_wait(&getting_forks_cond, &cond_mutex);

        eating_now++;
    } while (res);
    pthread_mutex_unlock(&cond_mutex);
}

void down_forks(int f1, int f2)
{
    pthread_mutex_lock(&cond_mutex);
    pthread_mutex_unlock(&forks[f1]);
    pthread_mutex_unlock(&forks[f2]);
    eating_now--;
    pthread_cond_broadcast(&getting_forks_cond);
    pthread_mutex_unlock(&cond_mutex);
}