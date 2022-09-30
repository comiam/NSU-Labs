#ifndef HUFFMAN_ENCODER_H
#define HUFFMAN_ENCODER_H

#include "tree.h"
#include <stdio.h>

Node** getFreqTable(FILE* file);
char** getCodeTable(Node* headTree, int height, char** codes, char* bcode);
void bitEncode(FILE *f0, FILE *f1, Node *headTree, char **codeTable);
unsigned long getCompressedDataSize(Node* headTree, char** codeTable);

#endif //HUFFMAN_ENCODER_H
