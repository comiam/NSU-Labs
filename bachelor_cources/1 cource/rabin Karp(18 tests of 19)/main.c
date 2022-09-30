#include <stdio.h>
#include <string.h>
#include <locale.h>
#include <math.h>
#include <stdlib.h>

int rabinKarp(unsigned char *s, unsigned char *sub, int alreadyWas, int last, int countOut)
{
    int pattern_hash = 0;
    int text_hash = 0;
    int chtr = 0;
    int was = 0;

    for (int i = 0; i < strlen(sub); i++)
    {
        pattern_hash += (sub[i] % 3) * (int) pow(3, i);
        text_hash += (s[i] % 3) * (int) pow(3, i);
    }

    for (int i = 0; i < strlen(s) - strlen(sub) + 1; ++i)
    {
        if (pattern_hash == text_hash)
        {
            chtr = 1;
            for (int j = 0; j < strlen(sub); ++j)
            {
                if (sub[j] != s[i + j])
                {
                    chtr = 0;
                    break;
                }
            }
            if (chtr == 0)
            {
                if (alreadyWas == 0)
                    printf("%i ", pattern_hash);
                was++;
                for (int j = 0; j < strlen(sub); ++j)
                {
                    printf("%i ", i + 1 + j + countOut);
                    if (sub[j] != s[i + j])
                        break;
                }
            } else
            {
                if (was == 0 && alreadyWas == 0)
                    printf("%i ", pattern_hash);
                for (int j = 0; j < strlen(sub); ++j)
                    printf("%i ", i + 1 + j + countOut);
                was++;
                chtr = 0;
            }
        }

        if (i < strlen(s) - strlen(sub))
        {
            text_hash -= (s[i] % 3);
            text_hash -= (text_hash / 3) * 2;
            text_hash += (int) ((s[i + strlen(sub)] % 3) * pow(3, strlen(sub) - 1));
        }
    }
    if (was == 0 && last)
        printf("%d", 0);
    if(!was)
        return 0;
    else
        return 1;
}

int main()
{
    setlocale(LC_ALL, "Russian");
    FILE *input = fopen("in.txt", "r");

    unsigned char buf[4096 + 2];
    unsigned char subs[128];
    short first = 0;
    short was = 0;
    fgets(subs, 127, input);
    for (int i = 0; i < strlen(subs); ++i)
        if ((subs[i] == '\n' && i == strlen(subs) - 1) || subs[i] == '\r')
            subs[i] = '\0';
    unsigned int d = 0;

    while (fread(buf, sizeof(char), 4096, input) == 4096)
    {
        buf[4096 + 1] = '\0';
        if(!first)
        {
            was = (short)rabinKarp(buf, subs, 0, 0, d++ * 4096);
            first++;
        }
        else
            was = (short)rabinKarp(buf, subs, was >= 1 ? 1 : 0, 0, d++ * 4096);
    }
    /*FILE* a = fopen("ans.txt", "w");
    for (int k = 0; k < strlen(buf); ++k)
    {
        fprintf(a, "%c", buf[k]);
    }
    fclose(a);*/
    //fread(currentPt, sizeof(char), size, input);
    fclose(input);

    for (int i = 0; i < strlen(buf); ++i)
        if ((buf[i] == '\n' && i == strlen(buf) - 1) || buf[i] == '\r')
            buf[i] = '\0';
    buf[strlen(buf)] = '\0';
    rabinKarp(buf, subs, 0, 1, d * 4096);
    return 0;
}