#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void* helloWorld(void *args) {
    for (int i = 0; i < 10; ++i)
        printf("+ kuk\n");

    return 5;
}

int main()
{
    pthread_t child;
    pthread_create(&child, NULL, helloWorld, NULL);

    for (int i = 0; i < 10; ++i)
        printf("- lel\n");

    pthread_exit(0);
}

