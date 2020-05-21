#include <io.h>
#include <fcntl.h>
#include <stdio.h>
#include <conio.h>

int wrongArgs()
{
    printf("Wrong args!!!\n");
    return 0;
}

int fileNotExist(char* fileName)
{
    printf("File doesn't exist: %s\n", fileName);
    return 0;
}

int main(int argc, char* argv[])
{
    if(argc < 2)
        return 0;

    FILE* input = stdin;
    FILE* output = stdout;
    if(argc > 2)
    {
        int wasInput = 0;
        int wasOutput = 0;

        for (int i = 2; i < 6; ++i)
        {
            if(!strcmp(argv[i], "-i") && i != 5 && !wasInput)
            {
                wasInput++;
                input = fopen(argv[++i], "rb");
                if(input == NULL)
                    return fileNotExist(argv[i - 1]);
                if(i == 5)
                    break;
            }else if(!strcmp(argv[i], "-o") && i != 5 && !wasOutput)
            {
                wasOutput++;
                output = fopen(argv[++i], "wb");
                if(output == NULL)
                    return fileNotExist(argv[i - 1]);
                if(i == 5)
                    break;
            }else
                return wrongArgs();

        }
    }
    _setmode(_fileno(stdin), _O_BINARY);
    _setmode(_fileno(stdout), _O_BINARY);

    size_t sizeBuf = 1024;
    char buf[sizeBuf];
    char* key = argv[1];

    size_t i = 0;
    size_t ind = 0;
    size_t keySize = strlen(key);

    while ((i = fread(buf, sizeof(char), sizeBuf, input)) != 0)
    {
        for (int j = 0; j < i; ++j)
        {
            buf[j] ^= key[ind];
            ind = (ind + 1) % keySize;
        }
        fwrite(buf, sizeof(char), i, output);
    }
    return 0;
}