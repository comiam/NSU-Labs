#include <string.h>
#include <stdlib.h>
#include "utils.h"
#include "encoder.h"
#include "decoder.h"
#include <fcntl.h>

int huffman(char* code, FILE* f0, FILE* f1);

int main(int argc, char* argv[])
{
    FILE* f0 = NULL;
    FILE* f1 = NULL;
    char* code;

    if(argc == 4)
    {
        code = argv[1];
        f0 = fopen(argv[2], "rb");
        f1 = fopen(argv[3], "wb");
    }
    else if(argc == 1)
    {
        char buf[DEFAULT_BLOCK_SIZE];

        f0 = fopen("in.txt", "r");
        FILE* temp = fopen("temp", "w");

        _setmode(_fileno(f0), _O_BINARY);
        _setmode(_fileno(temp), _O_BINARY);

        fread(buf, 1, 3, f0);
        char mode = buf[0];

        size_t i = 0;
        while ((i = fread(buf, 1, DEFAULT_BLOCK_SIZE, f0)) > 0)
            fwrite(buf, i, 1, temp);

        fclose(f0);
        fclose(temp);

        code = malloc(2 * sizeof(char));
        code[0] = mode;
        code[1] = '\0';

        f0 = fopen("temp", "rb");
        f1 = fopen("out.txt", "wb");
    }
    else
        argError

    _setmode(_fileno(f0), _O_BINARY);
    _setmode(_fileno(f1), _O_BINARY);

    return huffman(code, f0, f1);
}

int huffman(char* code, FILE* f0, FILE* f1)
{
    if(!f0 || !f1)
    {
        fileError
        if(f0)
            fclose(f0);
        if(f1)
            fclose(f1);

        return 0;
    }
    if(strcmp("-c", code) && strcmp("-d", code) && strcmp("c", code) && strcmp("d", code))
    {
        fclose(f0);
        fclose(f1);
        argError
    }
    if(!strcmp("-c", code) || !strcmp("c", code))
    {
        Node** freqTable = getFreqTable(f0);
        Node* headTree = getTree(freqTable);
        char** codeTable = getCodeTable(headTree, 0, NULL, NULL);

        /*FILE* f2 = fopen("onx", "wb");

        for (int j = 0; j < 256; ++j)
            if(codeTable[j])
                fprintf(f2, "%c %s\n", (unsigned char)j, codeTable[j]);*/

        bitEncode(f0, f1, headTree, codeTable);

        clearTree(headTree);

        for (int i = 0; i < 256; ++i)
            if(codeTable[i])
                free(codeTable[i]);
        free(codeTable);
    }else if(!strcmp("-d", code) || !strcmp("d", code))
    {
        unsigned char packageByte = 0;
        int packageIndex = 0;
        fread(&packageByte, sizeof(char), 1, f0);

        int tail = getTail(f0, &packageByte, &packageIndex);
        checkErrorReading(fclose(f0), fclose(f1), ;)

        //printf("%i\n", tail);

        //int i = 0;
        Node* headTree = readTree(f0, &packageByte, &packageIndex);
        checkErrorReading(fclose(f0), fclose(f1), clearTree(headTree))

        //char** codeTable = getCodeTable(headTree, 0, NULL, NULL);

        /*FILE* f3 = fopen("tw", "wb");

        for (int j = 0; j < 256; ++j)
            if(codeTable[j])
                fprintf(f3, "%c %s\n", (unsigned char)j, codeTable[j]);*/

        bitDecode(f0, f1, &packageByte, &packageIndex, headTree, tail);
        checkErrorReading(fclose(f0), fclose(f1), clearTree(headTree))

        clearTree(headTree);
    }

    if(!strcmp("d", code) || !strcmp("c", code))
        free(code);
    fclose(f0);
    fclose(f1);
    
    return 0;
}
