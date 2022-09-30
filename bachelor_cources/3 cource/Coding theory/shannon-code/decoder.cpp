#include "decoder.h"

int nextByteIsEOF(std::ifstream &f0)
{
    return f0.peek() == std::ifstream::traits_type::eof() && f0.eof();
}

unsigned int getTail(std::ifstream &f0, unsigned char *readByte, int *currentIndex)
{
    unsigned int value = 0;
    for (int i = 0; i < 3; ++i)
    {
        value <<= (unsigned int)1;
        value += readBit(f0, readByte, currentIndex);
        if(checkError())
            return 0;
    }
    return value;
}

void bitDecode(std::ifstream &f0, std::ofstream &f1, unsigned char* readByte, int* currentIndex, Node* tree, int tail)
{
    unsigned char buf;
    unsigned long long size = 0;
    int lastSizeCode;

    while(true)
    {
        lastSizeCode = 0;

        buf = readCode(f0, tree, readByte, currentIndex, &lastSizeCode);

        if(checkError())
            return;
        else
        {
            size += lastSizeCode;
            f1.write(reinterpret_cast<const char *>(&buf), 1);
        }

        if(size % 8 == tail && nextByteIsEOF(f0))
        {
            returnError();
            break;
        }
    }
}

unsigned char readCode(std::ifstream &f0, const Node* tree, unsigned char *readBytev, int *currentIndex, int* lengthBuf)
{
    Node node = *tree;
    while(node.left || node.right)
    {
        if(readBit(f0, readBytev, currentIndex))
        {
            if(checkError())
                return 0;
            if(!node.right)
            {
                throwError();
                return  0;
            }
            node = *node.right;
        }else
        {
            if(checkError())
                return 0;

            if(!node.left)
            {
                throwError();
                return  0;
            }
            node = *node.left;
        }
        (*lengthBuf)++;
    }
    return node.symbol;
}
