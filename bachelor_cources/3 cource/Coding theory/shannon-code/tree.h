#ifndef SHANNON_TREE_H
#define SHANNON_TREE_H

#include <cstdio>
#include <cstdlib>
#include <map>
#include <string>
#include <vector>

typedef struct node {
    unsigned long frequency;
    struct node* left;
    struct node* right;
    unsigned char symbol;
    double probability;
} Node;

Node* getTree(Node* symbolData, std::map<char, std::string> *codes);
void writeTree(std::ofstream &f0, Node* headTree, unsigned char* package, int* index);
Node* readTree(std::ifstream &f0, unsigned char* readByte, int* currentIndex);
void clearTree(Node* headTree);

#endif
