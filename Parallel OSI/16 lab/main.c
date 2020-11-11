#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

typedef struct node
{
    struct node *next;
    char *val;
} Node;

int returnFromThreads = 0;
pthread_t threadSorter;
pthread_mutex_t mutex;

void freeList(Node *head)
{
    Node *next;
    for (Node *node = head; node; node = next)
    {
        next = node->next;
        free(node);
    }
}

void destroyMutex()
{
    if (pthread_mutex_destroy(&mutex))
    {
        perror("destroying mutex");
        exit(EXIT_FAILURE);
    }
}

void stop(Node *head, char *errorMsg)
{
    perror(errorMsg);
    destroyMutex();
    freeList(head);
    exit(EXIT_FAILURE);
}

void lockMutex(Node *head)
{
    if (pthread_mutex_lock(&mutex))
        stop(head, "locking mutex");
}

void unlockMutex(Node *head)
{
    if (pthread_mutex_unlock(&mutex))
        stop(head, "unlocking mutex");
}

int listSize(Node **head)
{
    int size = 0;
    for (Node *node = (*head); node; node = node->next, size++);

    return size;
}

Node *createNode(char *str)
{
    Node *node = malloc(sizeof(Node));
    if (!node)
    {
        free(node);
        printf("malloc\n");
        return NULL;
    }

    node->next = NULL;
    node->val = malloc(strlen(str) + 1);
    if (!strcpy(node->val, str))
    {
        free(node->val);
        free(node);
        perror("creating node for string");
        return NULL;
    }

    return node;
}

void pushToList(Node **head, char *sentence)
{
    lockMutex(*head);

    Node *oldHead = (*head);
    Node *node = createNode(sentence);

    if (!node)
        return;

    (*head) = node;
    (*head)->next = oldHead;

    unlockMutex(*head);
}

void swap(Node* ptr1, Node* ptr2)
{
    Node* tmp = ptr2->next;
    ptr2->next = ptr1;
    ptr1->next = tmp;
}

int compare(char *left, char *right)
{
    size_t leftLen = strlen(left), rightLen = strlen(right);

    size_t minLength = (leftLen > rightLen) ? rightLen : leftLen;
    size_t maxLength = (leftLen < rightLen) ? rightLen : leftLen;

    for (size_t i = 0; i < minLength; ++i)
        if (left[i] != right[i])
            return 2 * (left[i] > right[i]) - 1;

    if (!(maxLength - minLength))
        return 0;

    return 2 * (maxLength == strlen(left)) - 1;
}

void bubbleSort(Node** head)
{
    Node** step;
    int swapped;

    for (int i = 0; i <= listSize(head); i++)
    {
        step = head;
        swapped = 0;

        for (int j = 0; j < listSize(head) - i - 1; j++)
        {
            Node* p2 = (*step)->next;

            if (compare((*step)->val, p2->val) > 0)
            {
                swap(*step, p2);
                swapped = 1;
            }

            step = &(p2->next);
        }

        if (!swapped)
            break;
    }
}

void initMutex()
{
    if (pthread_mutex_init(&mutex, NULL))
    {
        perror("creating mutex");
        exit(EXIT_FAILURE);
    }
}

void printList(Node **head)
{
    lockMutex(*head);

    printf("=================LIST=====================\n");
    for (Node *node = (*head); node; node = node->next)
        printf("%s\n", node->val);
    printf("=================END-LIST=================\n");

    unlockMutex(*head);
}

void sortStop(Node *head, int res)
{
    returnFromThreads = 1;
    if (!res)
    {
        unlockMutex(head);
        pthread_cancel(threadSorter);
    }
}

void *threadHandler(void *data)
{
    Node **head = (Node **) data;
    while (!returnFromThreads)
    {
        if (sleep(5))
            stop(*head, "sleeping");

        lockMutex(*head);

        printf("sorting...\n");

        if (!*head)
        {
            printf("empty list, skip...\n");
            unlockMutex(*head);

            continue;
        }

        bubbleSort(head);

        printf("end of sorting...\n");

        unlockMutex(*head);
    }

    return data;
}

void createSorterThread(Node **head)
{
    initMutex();

    if (pthread_create(&threadSorter, NULL, threadHandler, (void *) head))
        stop((*head), "creating thread");
}

ssize_t promptLine(char *line, int sizeLine)
{
    ssize_t n = 0;

    while (1)
    {
        n += read(0, (line + n), (size_t) (sizeLine - n));
        *(line + n) = '\0';

        return n;
    }
}

void getStrings(Node **head)
{
    char buf[81];
    ssize_t size;

    while (1)
    {
        size = promptLine(buf, 80);
        if (!strcmp(buf, "exit\n") || !size)
        {
            sortStop(*head, pthread_mutex_trylock(&mutex));
            return;
        } else if (!strcmp(buf, "\n"))
            printList(head);
        else
        {
            for (int i = 0; i < strlen(buf); i++)
                if (buf[i] == '\n')
                    buf[i] = '\0';

            pushToList(head, buf);
        }
    }
}

int main()
{
    Node *head = NULL;
    createSorterThread(&head);

    getStrings(&head);

    pthread_join(threadSorter, NULL);

    destroyMutex();
    freeList(head);
    return EXIT_SUCCESS;
}