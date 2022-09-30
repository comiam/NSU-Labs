#include "tree.h"
#include <stdlib.h>
#include "utils.h"

void clearTree(Node* headTree)
{
    if(headTree == NULL)
        return;

    clearTree(headTree->right);
    clearTree(headTree->left);
    free(headTree);
}

int bitSizeTree(char** codeTable)
{
    int size = 0;
    for (int i = 0; i < 256; ++i)
        if(codeTable[i])
            size++;
    return size * 10 - 1;
}

Node* readTree(FILE* f0, unsigned char* readBytev, int* currentIndex/*, int size, int* read*/)
{
    //if((*read) == size)
    //    return NULL;

    if (readBit(f0, readBytev, currentIndex))
    {
        if(checkError())
            return NULL;

        Node* new = calloc(1, sizeof(Node));
        new->symbol = readByte(f0, readBytev, currentIndex);

        if(checkError())
            return NULL;

        new->right = NULL;
        new->left = NULL;
        new->frequency = 0;
        //(*read) += 9;

        return new;
    }
    else
    {
        if(checkError())
            return NULL;

        //(*read) += 1;

        Node* left = readTree(f0, readBytev, currentIndex/*, size, read*/);
        if(checkError())
            return NULL;
        Node* right = readTree(f0, readBytev, currentIndex/*, size, read*/);
        if(checkError())
            return NULL;

        Node* new = calloc(1, sizeof(Node));
        new->right = right;
        new->left = left;
        new->frequency = 0;

        return new;
    }
}

Node* getTree(Node** freqTable)
{
    while(freqTable[0]->frequency != 1)//В данный момент freqTable[0]->frequency есть размер массива freqTable
    {
        qsort(freqTable + 1, freqTable[0]->frequency, sizeof(Node*), &comparator);

        Node* newNode = calloc(1, sizeof(Node));
        newNode->left = freqTable[1];
        newNode->right = freqTable[2];
        newNode->frequency = freqTable[1]->frequency + freqTable[2]->frequency;
        freqTable[2] = newNode;

        for (int i = 1; i < freqTable[0]->frequency; ++i)
            freqTable[i] = freqTable[i + 1];

        freqTable[0]->frequency--;
    }
    Node* head = freqTable[1];
    free(freqTable[0]);
    free(freqTable);

    return head;
}

void writeTree(FILE* f0, Node* headTree, unsigned char* package, int* index)
{
    if(!(headTree->right) && !(headTree->left))
    {
        writeBit(f0, 1, package, index);
        writeByte(f0, headTree->symbol, package, index);
    }else
    {
        writeBit(f0, 0, package, index);
        if(headTree->left)
            writeTree(f0, headTree->left, package, index);
        if(headTree->right)
            writeTree(f0, headTree->right, package, index);
    }
}