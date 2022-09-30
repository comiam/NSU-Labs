#ifndef HUFFMAN_DECODER_H
#define HUFFMAN_DECODER_H

#include "tree.h"

unsigned int getTail(FILE *f0, unsigned char *readByte, int *currentIndex);
void bitDecode(FILE* f0, FILE* f1, unsigned char* readByte, int* currentIndex, Node* tree, int tail);
unsigned char readCode(FILE *f0, const Node *tree, unsigned char *readBytev, int *currentIndex, int* lengthBuf);

#endif //HUFFMAN_DECODER_H
