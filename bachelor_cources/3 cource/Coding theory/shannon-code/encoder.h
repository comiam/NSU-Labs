#ifndef SHANNON_ENCODER_H
#define SHANNON_ENCODER_H

#include "tree.h"
#include "utils.h"
#include <string>
#include <algorithm>
#include <cmath>

Node* getProbTable(std::ifstream& file);
std::map<char, std::string>* getCodeTable(Node* symbolData);
void bitEncode(std::ifstream &f0, std::ofstream &f1, Node *headTree, std::map<char, std::string> *codeTable);
unsigned long getCompressedDataSize(Node* headTree, std::map<char, std::string>* codeTable);

#endif
