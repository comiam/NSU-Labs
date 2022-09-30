#include <stdio.h>
#include <malloc.h>
#include <stdlib.h>

typedef struct SingleListItem {
    struct SingleListItem* next;
    long index;
    long value;
} SingleItem;

typedef struct DoubleListItem {
    struct DoubleListItem* next;
    struct DoubleListItem* last;
    long index;
    long value;
} DoubleItem;

typedef struct SingleLinkedList {
    SingleItem* first;
    long size;
} SingleLinkedList;

typedef struct DoublyLinkedList {
    DoubleItem* first;
    long size;
} DoublyLinkedList;

DoublyLinkedList* createDouble(long size)
{
    DoublyLinkedList* list = (DoublyLinkedList*)malloc(sizeof(DoublyLinkedList));
    DoubleItem* last = NULL;
    list->size = size;
    for (int i = 0; i < size; ++i)
    {
        DoubleItem* item = (DoubleItem*)malloc(sizeof(DoubleItem));
        item->index = i;
        item->value = 0;
        if(last != NULL)
        {
            last->next = item;
            item->last = last;
        }
        else
        {
            list->first = item;
            item->last = NULL;
        }
        last = item;
        if(i == size - 1)
            item->next = NULL;
    }
    return list;
}

SingleLinkedList* createSingle(long size)
{
    SingleLinkedList* list = (SingleLinkedList*)malloc(sizeof(SingleLinkedList));
    SingleItem* last = NULL;
    list->size = size;
    for (int i = 0; i < size; ++i)
    {
        SingleItem* item = (struct SingleItem*)malloc(sizeof(SingleItem));
        item->index = i;
        item->value = 0;
        if(last != NULL)
            last->next = item;
        else
            list->first = item;
        last = item;
        if(i == size - 1)
            item->next = NULL;
    }
    return list;
}

SingleItem* getSingleItem(SingleLinkedList *list, long index)
{
    SingleItem* last = NULL;
    int was = 0;
    while(1)
    {
        if(last == NULL && !was)
        {
            last = list->first;
            was = 1;
        }
        if(last == NULL)
            return NULL;

        if(last->index == index)
            return last;
        else
            last = last->next;
    }
}

DoubleItem* getDoubleItem(DoublyLinkedList* list, long index)
{
    DoubleItem* last = NULL;
    int was = 0;
    while(1)
    {
        if(last == NULL && !was)
        {
            last = list->first;
            was = 1;
        }
        if(last == NULL)
            return NULL;

        if(last->index == index)
            return last;
        else
            last = last->next;
    }
}

void removeSingleItem(SingleLinkedList *list, long index)
{
    SingleItem* last = NULL;
    SingleItem* last1 = NULL;
    while(1)
    {
        if(last == NULL)
            last = list->first;
        if(last->index == index)
        {
            if(index != list->size - 1)
                for (int i = index + 1; i < list->size; ++i)
                    getSingleItem(list, i)->index--;

            if(last == list->first)
                list->first = last->next;
            else
                last1->next = last->next;

            free(last);
            list->size--;
            break;
        }else
        {
            last1 = last;
            last = last->next;
        }
    }
}

void removeDoubleItem(DoublyLinkedList* list, long index)
{
    DoubleItem* last = NULL;
    while(1)
    {
        if(last == NULL)
            last = list->first;
        if(last->index == index)
        {
            if(index != list->size - 1)
                for (int i = index + 1; i < list->size; ++i)
                    getDoubleItem(list, i)->index--;

            if(last->next != NULL && last->last != NULL)
            {
                last->last->next = last->next;
                last->next->last = last->last;
            }else if(last->next != NULL || last->last != NULL)
            {
                if(last->next == NULL)
                    last->last->next = NULL;
                else
                {
                    last->next->last = NULL;
                    list->first = last->next;
                }
            }

            free(last);
            last = NULL;
            list->size--;
            if(list->size == 0)
                list->first = NULL;
            break;
        }else
            last = last->next;
    }
}

void removeSingleList(SingleLinkedList *list)
{
    for (int i = list->size - 1; i >= 0; --i)
        removeSingleItem(list, i);
    free(list);
}

void removeDoublyList(DoublyLinkedList *list)
{
    for (int i = list->size - 1; i >= 0; --i)
        removeDoubleItem(list, i);
    free(list);
}

void printSingleList(SingleLinkedList *list)
{
    SingleItem* item = NULL;
    for (int i = 0; i < list->size; ++i)
    {
        item = getSingleItem(list, i);
        printf("index: %ld\nvalue: %ld\n//-----------\n", item->index, item->value);
    }
}

void printDoubleList(DoublyLinkedList *list)
{
    DoubleItem* item = NULL;
    for (int i = 0; i < list->size; ++i)
    {
        item = getDoubleItem(list, i);
        printf("index: %ld\nvalue: %ld\n//-----------\n", item->index, item->value);
    }
}

int main()
{
    {
        printf("Create SingleLinkedList--------------------------------------------\n");
        SingleLinkedList* list = createSingle(5);
        printSingleList(list);
        removeSingleList(list);
        printf("End tests of SingleLinkedList--------------------------------------\n");
    }
    {
        printf("Begin second task--------------------------------------------------\n");
        int step;
        int step0 = 0;
        int size;
        int i = 0;
        scanf("%d %d", &size, &step);
        SingleLinkedList* list = createSingle(size);
        for (int j = 0; j < list->size; ++j)
            getSingleItem(list, j)->value = j + 1;

        while(1)
        {
            if(list->size == 1)
                break;
            step0++;
            if(step0 == step)
            {
                removeSingleItem(list, i);
                step0 = 0;
                continue;
            }
            i = (i + 1)%(list->size);
        }
        printf("%ld\n", list->first->value);
        printf("End second task----------------------------------------------------\n");
    }
    {
        printf("Create DoublyLinkedList--------------------------------------------\n");
        DoublyLinkedList* list = createDouble(5);
        printDoubleList(list);
        removeDoublyList(list);
        printf("End tests of DoublyLinkedList--------------------------------------\n");
    }
    {
        printf("Begin third task--------------------------------------------------\n");
        int step;
        int step0 = 0;
        int size;
        int i = 0;
        scanf("%d %d", &size, &step);
        DoublyLinkedList* list = createDouble(size);
        for (int k = 0; k < list->size; ++k)
            getDoubleItem(list, k)->value = k + 1;

        while(1)
        {
            if(list->size == 1)
                break;
            step0++;
            if(step0 == step)
            {
                removeDoubleItem(list, i);
                step0 = 0;
                continue;
            }
            i = (i + 1)%(list->size);
        }
        printf("%ld\n", list->first->value);
        printf("End third task----------------------------------------------------\n");
    }
    return 0;
}