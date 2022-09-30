#ifndef SHANNON_UTILS_H
#define SHANNON_UTILS_H

#include <cstdio>
#include <fstream>

#define checkErrorReading(lol, lol2, lol3){if(checkError()){printf("Error during reading file in %s %d!\n", __FUNCTION__, __LINE__);lol;lol2;lol3; return 0;}};
#define argError {printf("Argument error!\n");return 0;};
#define fileError {printf("Error during opening file!");};
#define DEFAULT_BLOCK_SIZE 2048

int comparator(const void* a, const void* b);

void writeBit(std::ofstream &f0, unsigned char bit, unsigned char* package, int* packageIndex);
void writeByte(std::ofstream &f0, unsigned char byte, unsigned char* package, int* packageIndex);
void writeLastByte(std::ofstream &f0, unsigned char* package, int* packageIndex);

int readBit(std::ifstream &f0, unsigned char* package, int* packageIndex);
unsigned char readByte(std::ifstream &f0, unsigned char* package, int* packageIndex);
void gotoNextByte(std::ifstream &f0, unsigned char* package, int* packageIndex);

int checkError();
void throwError();
void returnError();

#endif
