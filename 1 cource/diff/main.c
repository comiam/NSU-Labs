#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <ctype.h>

#define noSuchFiles {printf("Error!\n");return 0;}
#define errorOpenFile {printf("Error during opening file!\n");return 0;}
#define DEFAULT_BLOCK_SIZE 1024

typedef struct pair
{
    long byteLine0;
    long byteLine1;
    long numLine0;
} Pair;

long getSize(FILE *f);
void matchSizes(long size0, long size1);
Pair *detectErrorLine(FILE *f1, FILE *f2);
int isTextMode(FILE *f0, FILE *f1);
int isText(char a);

void discrepancyText(FILE* file0, FILE* file1, Pair* err, long size0, long size1);
void printDiscrepancyText(FILE* file0, FILE* file1, Pair* err);
void printLine(FILE* file0, long set);
void printDiscrepancyLine(FILE* file0, FILE* file1, Pair* err);

void discrepancyBytes(FILE* file0, FILE* file1, long size0, long size1);
void printByteLine(long bytesReadTotal, size_t read, int k, unsigned char* buffer);
void printDiscrepancyByteLine(size_t read0, size_t read1, int k, const unsigned char* buffer, const unsigned char* buffer1);

int main(int argc, char *argv[])
{
    if (argc < 3)
        noSuchFiles

    FILE *file0 = fopen(argv[1], "rb");
    FILE *file1 = fopen(argv[2], "rb");

    if (file0 == NULL || file1 == NULL)
        errorOpenFile

    long size0 = getSize(file0);
    long size1 = getSize(file1);

    if (isTextMode(file0, file1))
    {
        Pair *err = detectErrorLine(file0, file1);
        if (!err)
            printf("OK!\n");
        else
        {
            discrepancyText(file0, file1, err, size0, size1);
            free(err);
        }
    } else
        discrepancyBytes(file0, file1, size0, size1);

    fclose(file0);
    fclose(file1);
    return 0;
}

void printDiscrepancyByteLine(size_t read0, size_t read1, int k, const unsigned char* buffer, const unsigned char* buffer1)
{
    div_t divN = div(k, 16);

    printf("         ");

    for (int m = k - divN.rem; m < (k - divN.rem + 16); ++m)
    {
        if ((m >= read0 || m >= read1) || (buffer[m] != buffer1[m]))
            printf("++ ");
        else
            printf("   ");
        if (m == (k - divN.rem + 7))
            printf("| ");
    }
}

void printByteLine(long bytesReadTotal, size_t read, int k, unsigned char* buffer)
{
    div_t divN = div((int) (bytesReadTotal + k), 16);
    printf("%08lX ", bytesReadTotal + k - divN.rem);
    divN = div(k, 16);

    for (int l = k - divN.rem; l < (k - divN.rem + 16); ++l)
    {
        if (l < read)
            printf("%02X ", buffer[l]);
        else
            printf("00 ");
        if (l == (k - divN.rem + 7))
            printf("| ");
    }
}

void discrepancyBytes(FILE* file0, FILE* file1, long size0, long size1)
{
    fseek(file0, 0, SEEK_SET);
    fseek(file1, 0, SEEK_SET);

    unsigned char* buf = (unsigned char*)malloc(DEFAULT_BLOCK_SIZE * sizeof(char));
    unsigned char* buf1 = (unsigned char*)malloc(DEFAULT_BLOCK_SIZE * sizeof(char));
    size_t i, j;
    long bytesReaded = 0;
    int mark = 0;

    while (1)
    {
        i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, file0);
        j = fread(buf1, sizeof(char), DEFAULT_BLOCK_SIZE, file1);

        if (i == 0 && j == 0)
            break;

        for (int k = 0; k < DEFAULT_BLOCK_SIZE; ++k)
        {
            if ((k == i && k != j) || (k == j && k != i) || (buf[k] != buf1[k]))
            {
                matchSizes(size0, size1);

                printf("Discrepancy at byte %li (%02X vs %02X)\n", bytesReaded + k, k == i && k != j ? 0 : buf[k],
                       k == j && k != i ? 0 : buf1[k]);

                printByteLine(bytesReaded, i, k, buf);
                putchar('\n');

                printDiscrepancyByteLine(i, j, k, buf, buf1);
                putchar('\n');

                printByteLine(bytesReaded, j, k, buf1);
                putchar('\n');

                mark = 1;
                break;
            }
        }
        if (mark)
            break;

        bytesReaded += DEFAULT_BLOCK_SIZE;
    }
    if (!mark)
        printf("OK!\n");
    free(buf);
    free(buf1);
}

void printDiscrepancyText(FILE* file0, FILE* file1, Pair* err)
{
    fseek(file0, err->byteLine0, SEEK_SET);
    fseek(file1, err->byteLine1, SEEK_SET);
    char buf[DEFAULT_BLOCK_SIZE];
    char buf1[DEFAULT_BLOCK_SIZE];

    size_t i, j;
    long tail = 0;
    int first = 0;

    while (1)
    {
        i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, file0);
        j = fread(buf1, sizeof(char), DEFAULT_BLOCK_SIZE, file1);
        for (int l = 0; l < (i > j ? j : i); ++l)
        {
            if (buf[l] != buf1[l])
            {
                if (buf[l] == '\n')
                    printf("Discrepancy at byte %li, at line %li('\\n' vs '%c')\n", tail + err->byteLine0 + l,
                           err->numLine0 + 1, j == 0 ? ' ' : buf1[l]);
                else if (buf1[l] == '\n')
                    printf("Discrepancy at byte %li, at line %li('%c' vs '\\n')\n", tail + err->byteLine0 + l,
                           err->numLine0 + 1, i == 0 ? ' ' : buf[l]);
                else
                    printf("Discrepancy at byte %li, at line %li('%c' vs '%c')\n", tail + err->byteLine0 + l,
                           err->numLine0 + 1, i == 0 ? ' ' : buf[l], j == 0 ? ' ' : buf1[l]);
                first = 1;
                break;
            }
        }
        if (!first && i != j)
        {
            if (buf[(i > j ? j : i)] == '\n')
                printf("Discrepancy at byte %li, at line %li('\\n' vs '%c')\n",
                       tail + err->byteLine0 + (i > j ? j : i), err->numLine0 + 1,
                       j == 0 ? ' ' : buf1[(i > j ? j : i)]);
            else if (buf1[(i > j ? j : i)] == '\n')
                printf("Discrepancy at byte %li, at line %li('%c' vs '\\n')\n",
                       tail + err->byteLine0 + (i > j ? j : i), err->numLine0 + 1,
                       i == 0 ? ' ' : buf[(i > j ? j : i)]);
            else
                printf("Discrepancy at byte %li, at line %li('%c' vs '%c')\n",
                       tail + err->byteLine0 + (i > j ? j : i), err->numLine0 + 1,
                       i == 0 ? ' ' : buf[(i > j ? j : i)],
                       j == 0 ? ' ' : buf1[(i > j ? j : i)]);
            break;
        } else if (first)
            break;
        tail += DEFAULT_BLOCK_SIZE;
    }
}

void printLine(FILE* file0, long set)
{
    fseek(file0, set, SEEK_SET);
    size_t i;
    char buf[DEFAULT_BLOCK_SIZE];

    while (1)
    {
        i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, file0);
        if (i == 0)
            break;
        for (int k = 0; k < i; ++k)
        {
            if (buf[k] == '\n')
                break;
            else
                putchar(buf[k]);
        }
    }
}

void printDiscrepancyLine(FILE* file0, FILE* file1, Pair* err)
{
    fseek(file0, err->byteLine0, SEEK_SET);
    fseek(file1, err->byteLine1, SEEK_SET);

    char buf[DEFAULT_BLOCK_SIZE];
    char buf1[DEFAULT_BLOCK_SIZE];

    size_t i, j, u;

    unsigned int firstEnd = 0;
    unsigned int secondEnd = 0;
    while (1)
    {
        i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, file0);
        j = fread(buf1, sizeof(char), DEFAULT_BLOCK_SIZE, file1);
        u = 0;

        if (i == 0 && j == 0)
            break;

        if (i == 0)
            firstEnd = 1;

        if (j == 0)
            secondEnd = 1;

        while (u < DEFAULT_BLOCK_SIZE)
        {
            if (buf[u] == '\n')
            {
                firstEnd = 1;
                if (secondEnd)
                    break;
            }

            if (buf1[u] == '\n')
            {
                secondEnd = 1;
                if (firstEnd)
                    break;
            }

            if ((!firstEnd && !secondEnd && buf[u] != buf1[u] && buf[u] != '\n' && buf1[u] != '\n') ||
                (firstEnd ^ secondEnd) || (u >= i || u >= j))
                putchar('+');
            else
                putchar(' ');

            u++;
        }
    }
}

void discrepancyText(FILE* file0, FILE* file1, Pair* err, long size0, long size1)
{
    matchSizes(size0, size1);

    printDiscrepancyText(file0, file1, err);
    printLine(file0, err->byteLine0);

    putchar('\n');

    printDiscrepancyLine(file0, file1, err);

    putchar('\n');

    printLine(file1, err->byteLine1);

    putchar('\n');
}

int isTextMode(FILE *f0, FILE *f1)
{
    int size = 0;
    char *buf = (char *) malloc(DEFAULT_BLOCK_SIZE * sizeof(char));

    while ((size = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, f0)) != 0)
        for (int i = 0; i < size; ++i)
            if (!isText(buf[i]))
            {
                free(buf);
                return 0;
            }

    while ((size = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, f1)) != 0)
        for (int i = 0; i < size; ++i)
            if (!isText(buf[i]))
            {
                free(buf);
                return 0;
            }
    return 1;
}

Pair *detectErrorLine(FILE *f1, FILE *f2)
{
    fseek(f1, 0, SEEK_SET);
    fseek(f2, 0, SEEK_SET);
    long byteLine0Ind = 0, byteLine1Ind = 0;
    long numLine0Ind = 0, numLine1Ind = 0;
    size_t i, j, k;
    long read = 0;

    char *buf = (char *) malloc(DEFAULT_BLOCK_SIZE * sizeof(char));
    char *buf1 = (char *) malloc(DEFAULT_BLOCK_SIZE * sizeof(char));

    while (1)
    {
        i = fread(buf, sizeof(char), DEFAULT_BLOCK_SIZE, f1);
        j = fread(buf1, sizeof(char), DEFAULT_BLOCK_SIZE, f2);
        k = 0;

        if (i == 0 && j == 0)
        {
            free(buf);
            free(buf1);
            return NULL;
        }

        while (k++ < DEFAULT_BLOCK_SIZE)
        {
            if ((k == i && k != j) || (k == j && k != i) || (buf[k] != buf1[k]))
            {
                Pair *a = (Pair *) malloc(sizeof(Pair));
                a->byteLine0 = byteLine0Ind;
                a->byteLine1 = byteLine1Ind;
                a->numLine0 = numLine0Ind;
                free(buf);
                free(buf1);
                return a;
            }
            if (buf[k] == '\n')
            {
                byteLine0Ind = read + k + 1;
                numLine0Ind++;
            }

            if (buf1[k] == '\n')
            {
                byteLine1Ind = read + k + 1;
                numLine1Ind++;
            }

        }
        read += DEFAULT_BLOCK_SIZE;
    }
}

void matchSizes(long size0, long size1)
{
    printf("Size %s: file 1 is %li, file 2 is %li\n", size0 == size1 ? "match" : "mismatch", size0, size1);
}

int isText(char a)
{
    if (isprint(a))
        return 1;
    else if (a == '\n' || a == '\r')
        return 1;
    else
        return 0;
}

long getSize(FILE *f)
{
    fseek(f, 0, SEEK_END);
    long size0 = ftell(f);
    fseek(f, 0, SEEK_SET);
    return size0;
}