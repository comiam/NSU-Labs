#include <stdio.h>
#include <string.h>
#include <ctype.h>

void getBytes(char* argv);
void getLines(char* argv);
void getWords(char* argv);

int main(int argc, char* argv[])
{
    if(argc == 2)
    {
        getBytes(argv[1]);
        getLines(argv[1]);
        getWords(argv[1]);
    }else if(argc == 3)
    {
        if(!strcmp(argv[1], "-l"))
            getLines(argv[2]);
        else if(!strcmp(argv[1], "-w"))
            getWords(argv[2]);
        else if(!strcmp(argv[1], "-c"))
            getBytes(argv[2]);
        else printf("Ti durak??\n");
    }
    return 0;
}

void getWords(char* argv)
{
    FILE* in = fopen(argv, "r");
    char buf[1024] = {0};
    long size = 0;
    short afterSymbol = 0;
    size_t u = 0;
    while((u = fread(buf, sizeof(char), 1024, in)) == 1024)
    {
        for (int i = 0; i < 1024; ++i)
            if((isspace(buf[i]) || buf[i] == '\n') && afterSymbol)
            {
                size++;
                afterSymbol = 0;
            }
            else if((isalpha(buf[i]) || ispunct(buf[i]) || (buf[i] >= '0' && buf[i] <= '9')) && buf[i] != '\n')
                afterSymbol = 1;
    }
    for (int i = 0; i < u; ++i)
        if((isspace(buf[i]) || buf[i] == '\n') && afterSymbol)
        {
            size++;
            afterSymbol = 0;
        }
        else if((isalpha(buf[i]) || ispunct(buf[i]) || (buf[i] >= '0' && buf[i] <= '9')) && buf[i] != '\n')
            afterSymbol = 1;
    if(afterSymbol)
        size++;
    printf("Words: %li\n", size);
    fclose(in);
}

void getLines(char* argv)
{
    FILE* in = fopen(argv, "r");
    char buf[1024] = {0};
    long size = 0;
    size_t u = 0;
    short afterSymbol = 0;
    while((u = fread(buf, sizeof(char), 1024, in)) == 1024)
        for (int i = 0; i < 1024; ++i)
            if(buf[i] == '\n')
                size++;
            else if(isalpha(buf[i]) || ispunct(buf[i]) || isspace(buf[i]))
                afterSymbol = 1;
    for (int i = 0; i < u; ++i)
        if(buf[i] == '\n')
        {
            size++;
            afterSymbol = 0;
        }
        else if(isalpha(buf[i]) || ispunct(buf[i]) || isspace(buf[i]))
            afterSymbol = 1;
    if(afterSymbol)
        size++;
    printf("Lines: %li\n", size);
    fclose(in);
}

void getBytes(char* argv)
{
    FILE* in = fopen(argv, "rb");
    char buf[1024] = {0};
    size_t i = 0;
    unsigned long size = 0;
    while((i = fread(buf, sizeof(char), 1024, in)) == 1024)
        size += i;
    printf("Bytes: %lu\n",(unsigned long)((size + i) * sizeof(char)));
    fclose(in);
}