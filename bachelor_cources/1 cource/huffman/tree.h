#ifndef HUFFMAN_TREE_H
#define HUFFMAN_TREE_H

#include <stdio.h>

typedef struct node {
    unsigned char symbol;
    unsigned long frequency;
    struct node* left;
    struct node* right;
} Node;

Node* getTree(Node** freqTable);
void writeTree(FILE* f0, Node* headTree, unsigned char* package, int* index);
Node* readTree(FILE* f0, unsigned char* readByte, int* currentIndex/*, int size, int* read*/);
void clearTree(Node* headTree);

#endif //HUFFMAN_TREE_H
