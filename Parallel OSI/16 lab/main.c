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

Node *head = NULL;
int returnFromThreads = 0;
pthread_t threadSorter;
pthread_mutex_t mutex;

int listSize() 
{
	int size = 0;
	for(Node* node = head;node;node = node->next, size++);

	return size;	
}

Node* createNode(char* str) 
{
    Node* node = malloc(sizeof(Node));
    if (!node) 
    {
        free(node);
        printf("malloc\n");
        return NULL;
    }

    node->next = NULL;
    node->val = malloc(strlen(str) + 1);
    if(!strcpy(node->val, str))
    {
        free(node->val);
        free(node);
        perror("creating node for string");
        return NULL;
    }

    return node;
}

void pushToList(char* sentence) 
{
	Node *oldHead = head;
	Node *node = createNode(sentence);

	if(!node)
		return;

	head = node;
	head->next = oldHead;
}

void freeList() 
{
	Node* next;
	for(Node* node = head;node;node = next)
	{
		next = node->next;
		free(node);
	}
}

void swap(char** left, char** right)
{
    char* tmp = *left;
    *left = *right;
    *right = tmp;
}

void initMutex()
{
	if(pthread_mutex_init(&mutex, NULL))
	{
        perror("creating mutex");
        exit(EXIT_FAILURE);
    }
}

void destroyMutex()
{
    if(pthread_mutex_destroy(&mutex))
    {
        perror("destroying mutex");
        exit(EXIT_FAILURE);
    }
}

void stop(char* errorMsg)
{
    perror(errorMsg);
    destroyMutex();
    freeList();
    exit(EXIT_FAILURE);
}

void lockMutex()
{
    if(pthread_mutex_lock(&mutex))
         stop("locking mutex");
}

void unlockMutex()
{
    if(pthread_mutex_unlock(&mutex))
        stop("unlocking mutex");
}

void printList()
{
    lockMutex();
    printf("=================LIST=================\n");
    for(Node* node = head;node;node = node->next)
        printf("%s\n", node->val);
    printf("=================END-LIST=================\n");
    unlockMutex();
}

void sortStop()
{
    returnFromThreads = 1;
}

int compare(char* left, char* right)
{
    size_t leftLen = strlen(left), rightLen = strlen(right);

    size_t minLength = (leftLen > rightLen) ? rightLen : leftLen;
    size_t maxLength = (leftLen < rightLen) ? rightLen : leftLen;

    for(size_t i = 0; i < minLength; ++i)
        if(left[i] != right[i])
            return 2 * (left[i] > right[i]) - 1;
        
    if (!(maxLength - minLength))
        return 0;
    
    return 2 * (maxLength == strlen(left)) - 1;
}

void *threadHandler(void *data)
{
    while(!returnFromThreads)
    {
    	if(sleep(5))
            stop("sleeping");
        
        lockMutex();
        printf("sorting...\n");

        if(!head) 
        {
        	printf("empty list, skip...\n");
        	unlockMutex();

        	continue;
        }

        int j = 0;
        for(int i = 0; i < listSize(); ++i, j = 0)
            for(Node* node = head; j < listSize() - i - 1; node = node ->next, ++j)
                if(compare(node->val, node->next->val) > 0)
                    swap(&(node->val), &(node->next->val));
    	
    	printf("end of sorting...\n");
    	unlockMutex();
    }

    return data;
}

void createSorterThread()
{
    initMutex();

    if(pthread_create(&threadSorter,NULL, threadHandler, NULL))
        stop("creating thread");
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

void getStrings()
{
    char buf[81];
    ssize_t size;

    while(1)
    {
        size = promptLine(buf, 80);
        if(!strcmp(buf,"exit\n") || !size)
        {
            sortStop();
            return;
        }else if(!strcmp(buf,"\n"))
            printList();
        else
        {
            lockMutex();
            for(int i = 0;i< strlen(buf);i++)
                if(buf[i] == '\n')
                    buf[i] = '\0';

            pushToList(buf);
            unlockMutex();
        }
    }
}

int main()
{
    createSorterThread();

    getStrings();

    pthread_join(threadSorter, NULL);

    destroyMutex();
    freeList();
    return EXIT_SUCCESS;
}