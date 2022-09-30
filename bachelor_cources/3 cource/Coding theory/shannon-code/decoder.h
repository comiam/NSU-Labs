#ifndef SHANNON_DECODER_H
#define SHANNON_DECODER_H

#include "tree.h"
#include "utils.h"

unsigned int getTail(std::ifstream &f0, unsigned char *readByte, int *currentIndex);
void bitDecode(std::ifstream &f0, std::ofstream &f1, unsigned char* readByte, int* currentIndex, Node* tree, int tail);
unsigned char readCode(std::ifstream &f0, const Node *tree, unsigned char *readBytev, int *currentIndex, int* lengthBuf);

#endif
