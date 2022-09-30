#include "utils.h"
#include "tree.h"

void checkAndFlushByte(FILE* f0, unsigned char* package, int* packageIndex);
void checkAndReadByte(FILE* f0, unsigned char* package, int* packageIndex);

int ERROR_READING = 0;
unsigned char aheadByte;
int hAheadByte = 0;

int haveAheadByte()
{
    return hAheadByte;
}

void setAHeadByte(unsigned char byte)
{
    aheadByte = byte;
    hAheadByte = 1;
}

int checkError()
{
    return ERROR_READING;
}

void throwError()
{
    ERROR_READING = 1;
}

void returnError()
{
    ERROR_READING = 0;
}

int comparator(const void* a, const void* b)
{
    return (*((Node**)a))->frequency - (*((Node**)b))->frequency;
}

int readBit(FILE* f0, unsigned char* package, int* packageIndex)
{
    checkAndReadByte(f0, package, packageIndex);
    int bit = ((*package) >> (unsigned char)(8 - ((*packageIndex) + 1))) % 2;
    (*packageIndex)++;
    return bit;
}

unsigned char readByte(FILE* f0, unsigned char* package, int* packageIndex)
{
    unsigned char value = 0;
    for (int i = 0; i < 8; ++i)
    {
        value <<= (unsigned char)1;
        value += readBit(f0, package, packageIndex);
    }
    return value;
}

void writeLast(FILE* f0, unsigned char* package, int* packageIndex)
{
    if((*packageIndex) != 0)
    {
        (*package) <<= (unsigned char)(8 - (*packageIndex));
        fwrite(package, sizeof(char), 1, f0);
    }
}

void writeBit(FILE* f0, unsigned char bit, unsigned char* package, int* packageIndex)
{
    (*package) <<= (unsigned char)1;
    (*package) += bit;
    (*packageIndex)++;

    checkAndFlushByte(f0, package, packageIndex);
}

void writeByte(FILE* f0, unsigned char byte, unsigned char* package, int* packageIndex)
{
    for(int i = 7; i >= 0; --i)
        writeBit(f0, (byte >> (unsigned char)i) % 2, package, packageIndex);
}

void checkAndReadByte(FILE* f0, unsigned char* package, int* packageIndex)
{
    if((*packageIndex) == 8)
    {
        if(hAheadByte)
        {
            (*package) = aheadByte;
            hAheadByte = 0;
        }else if(!fread(package, sizeof(char), 1, f0))
            ERROR_READING = 1;
        (*packageIndex) = 0;
    }
}

void checkAndFlushByte(FILE* f0, unsigned char* package, int* packageIndex)
{
    if((*packageIndex) == 8)
    {
        fwrite(package, sizeof(char), 1, f0);
        (*packageIndex) = 0;
        (*package) = 0;
    }
}