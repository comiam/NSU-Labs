#include <stdio.h>
#include <pthread.h>

void* helloWorld(void *args) {
    for (int i = 0; i < 10; ++i)
        printf("+ kuk\n");
    return 0;
}

int main()
{
    pthread_t child;
    pthread_create(&child, NULL, helloWorld, NULL);

    for (int i = 0; i < 10; ++i)
        printf("- lel\n");

    pthread_join(child, NULL);

    return 0;
}

