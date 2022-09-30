#include "utils.h"
#include "tree.h"

void checkAndFlushByte(std::ofstream &f0, unsigned char* package, int* packageIndex);
void checkAndReadByte(std::ifstream &f0, unsigned char* package, int* packageIndex);

static int ERROR_READING = 0;

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
    return (*(Node*)a).probability < (*(Node*)b).probability ? 1 : -1;
}

int readBit(std::ifstream &f0, unsigned char* package, int* packageIndex)
{
    checkAndReadByte(f0, package, packageIndex);
    int bit = ((*package) >> (unsigned char)(8 - ((*packageIndex) + 1))) % 2;
    (*packageIndex)++;
    return bit;
}

void gotoNextByte(std::ifstream &f0, unsigned char* package, int* packageIndex)
{
    f0.read(reinterpret_cast<char*>(package), 1);
    int i = f0.gcount();

    if(!i)
        ERROR_READING = 1;
    (*packageIndex) = 0;
}

unsigned char readByte(std::ifstream &f0, unsigned char* package, int* packageIndex)
{
    unsigned char value = 0;
    for (int i = 0; i < 8; ++i)
    {
        value <<= (unsigned char)1;
        value += readBit(f0, package, packageIndex);
    }
    return value;
}

void writeLastByte(std::ofstream &f0, unsigned char* package, int* packageIndex)
{
    if((*packageIndex) != 0)
    {
        (*package) <<= (unsigned char)(8 - (*packageIndex));
        f0.write(reinterpret_cast<char*>(package), 1);
        (*packageIndex) = 0;
        (*package) = 0;
    }
}

void writeBit(std::ofstream &f0, unsigned char bit, unsigned char* package, int* packageIndex)
{
    (*package) <<= (unsigned char)1;
    (*package) += bit;
    (*packageIndex)++;

    checkAndFlushByte(f0, package, packageIndex);
}

void writeByte(std::ofstream &f0, unsigned char byte, unsigned char* package, int* packageIndex)
{
    for(int i = 7; i >= 0; --i)
        writeBit(f0, (byte >> (unsigned char)i) % 2, package, packageIndex);
}

void checkAndReadByte(std::ifstream &f0, unsigned char* package, int* packageIndex)
{
    if((*packageIndex) == 8)
    {
        f0.read(reinterpret_cast<char*>(package), 1);
        int i = f0.gcount();

        if (!i)
            ERROR_READING = 1;

        (*packageIndex) = 0;
    }
}

void checkAndFlushByte(std::ofstream &f0, unsigned char* package, int* packageIndex)
{
    if((*packageIndex) == 8)
    {
        f0.write(reinterpret_cast<char*>(package), 1);
        (*packageIndex) = 0;
        (*package) = 0;
    }
}