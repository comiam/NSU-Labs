#include "encoder.h"
#include "utils.h"
#include <malloc.h>
#include <stdlib.h>
#include <string.h>

void takeCode(char* code, int height, char** value, unsigned char codeV);

Node** getFreqTable(FILE* file)
{
    unsigned char buf[DEFAULT_BLOCK_SIZE];
    Node* table = (Node*)calloc(256, sizeof(Node));
    size_t i, k = 1;

    while((i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, file)) != 0)
        for (int j = 0; j < i; ++j)
        {
            table[buf[j]].frequency++;
            table[buf[j]].symbol = buf[j];
        }

    Node** finalTable = (Node**)realloc(NULL, k * sizeof(Node*));
    finalTable[0] = (Node*)calloc(1, sizeof(Node));

    for (int l = 0; l < 256; ++l)
    {
        if(table[l].frequency != 0)
        {
            finalTable = (Node**)realloc(finalTable, (++k) * sizeof(Node*));
            finalTable[k - 1] = (Node*)calloc(1, sizeof(Node));

            (*finalTable[k - 1]) = table[l];
            (*finalTable[k - 1]).left = NULL;
            (*finalTable[k - 1]).right = NULL;

            finalTable[0]->frequency = k - 1;
        }
    }
    free(table);

    return finalTable;
}

char** getCodeTable(Node* headTree, int height, char** codes, char* bcode)
{
    if(!codes)
    {
        codes = (char**)calloc(256, sizeof(char*));
        for (int i = 0; i < 256; ++i)
            codes[i] = NULL;
    }

    if(!bcode)
        bcode = calloc(256, sizeof(char));

    if(headTree->left)
    {
        bcode[height] = '0';
        getCodeTable(headTree->left, height + 1, codes, bcode);
    }

    if(headTree->right)
    {
        bcode[height] = '1';
        getCodeTable(headTree->right, height + 1, codes, bcode);
    }

    if(!(headTree->right) && !(headTree->left))
        takeCode(bcode, height, codes, headTree->symbol);

    if(!height)
        free(bcode);

    return codes;
}

void bitEncode(FILE *f0, FILE *f1, Node *headTree, char **codeTable)
{
    fseek(f0, 0, SEEK_SET);
    unsigned char packageByte = 0;
    int packageIndex = 0;

    //printf("%lu %lu\n", getCompressedDataSize(headTree, codeTable), getCompressedDataSize(headTree, codeTable) % 8);
    /*
    for (int l = 0; l < 4; ++l)
    {
        writeByte(f1, (unsigned char)sizeData, &packageByte, &packageIndex);
        sizeData >>= (unsigned long)8;
    }*/
    switch(getCompressedDataSize(headTree, codeTable) % 8)
    {
        case 0:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 1:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        case 2:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 3:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        case 4:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 5:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        case 6:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 7:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        default:
            printf("Error during writing!!!\n");
            return;
    }

    writeTree(f1, headTree, &packageByte, &packageIndex);

    unsigned char buf[DEFAULT_BLOCK_SIZE];
    size_t i, k = 0;

    while((i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, f0)) != 0)
        for (int j = 0; j < i; ++j)
        {
            while(codeTable[buf[j]][k] != '\0')
            {
                if(codeTable[buf[j]][k] == '0')
                    writeBit(f1, 0, &packageByte, &packageIndex);
                else if(codeTable[buf[j]][k] == '1')
                    writeBit(f1, 1, &packageByte, &packageIndex);
                k++;
            }
            k = 0;
        }
    writeLast(f1, &packageByte, &packageIndex);
}

void takeCode(char* code, int height, char** value, unsigned char codeV)
{
    if(!value[codeV])
        value[codeV] = (char*)malloc((height + 1) * sizeof(char));

    memcpy(value[codeV], code, height);
    value[codeV][height] = '\0';
}

unsigned long getCompressedDataSize(Node* headTree, char** codeTable)
{
    if(!headTree)
        return 0;
    if(!(headTree->right) && !(headTree->left))
        return strlen(codeTable[headTree->symbol]) * headTree->frequency;
    else
        return getCompressedDataSize(headTree->left, codeTable) + getCompressedDataSize(headTree->right, codeTable);
}