#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#define ERROR "bad input"
#define null NULL

int toNum(char n)
{
    if (n >= 'a' && n <= 'z')
        n += 'A' - 'a';
    return (n >= 'A' && n <= 'Z') ? (n - 'A' + 10) : (n - '0');
}

char toChar(int n) {
    return (char)((n >= 0 && n <= 9) ? ('0' + n) : ('A' - 10 + n));
}

void toDecimal(int ss0, char* num, unsigned long long* div, long double* fractional)
{
    int strSize = (int)strlen(num) - 1;
    if(strstr(num, ".") != null)
    {
        int dotI = (int)(strstr(num, ".") - num);
        for(int i = dotI - 1; i >= 0; --i)
        {
            (*div) += toNum(num[i]) * (unsigned long long)pow(ss0, dotI - 1 - i);
        }
        strSize++;
        for(int i = dotI + 1; i < strSize; ++i)
        {
            (*fractional) += toNum(num[i]) * pow(ss0, -(i - dotI));
        }
    }else
    {//printf("%c %d\n", num[i], toNum(num[i]));
        for(int i = strSize; i >= 0; --i)
        {
            (*div) += toNum(num[i]) * (unsigned long long)pow(ss0, strSize - i);
        }
    }
}

char* fromDecimal(int ss1, unsigned long long decimal, long double fractional)
{
    if(ss1 == 10)
    {
        char* res = (char*)malloc(80 * sizeof(char));
        if(fractional >= 0.000000000001)
            sprintf(res, "%lli.%Lf", decimal, fractional);
        else
            sprintf(res, "%lli", decimal);
        return res;
    }
    unsigned long long divPart = decimal;
    long double fraqPart = fractional;
    char *divPartStr = (char*)malloc(80 * sizeof(char)), *fraqPartStr = (char*)malloc(80 * sizeof(char));

    int i = 0;

    do {
        divPartStr[i++] = toChar(divPart % ss1);
        divPart /= ss1;
    } while (divPart);
    divPartStr[i] = '\0';

    int len = i;
    for (i = 0; i < len / 2; ++i) {
        char temp = divPartStr[i];
        divPartStr[i] = divPartStr[len - i - 1];
        divPartStr[len - i - 1] = temp;
    }

    i = 0;
    while (fraqPart > 0 && i < 13) {
        fraqPart *= ss1;
        divPart = (unsigned long long)fraqPart;
        fraqPart -= (long long int)fraqPart;
        fraqPartStr[i++] = toChar(divPart);
    }
    fraqPartStr[i] = '\0';
    char* res = (char*)malloc(80 * sizeof(char));
    if(i == 0)
    {
        sprintf(res, "%s\n", divPartStr);
    }else
    {
        sprintf(res, "%s.%s\n", divPartStr, fraqPartStr);
    }
    free(divPartStr);
    free(fraqPartStr);
    return res;
}

char* checkForSyntax(int ss0, int ss1, char* num)
{
    if(ss0 <= 1 || ss0 > 16 || ss1 <= 1 || ss1 > 16 || num == null || strlen(num) == 0)
        return ERROR;
    if(strstr(num, ".") != null && (int)(strstr(num, ".") - num) == strlen(num) - 1)
        return ERROR;
    if(strstr(num, ".") != null && (int)(strstr(num, ".") - num) == 0)
        return ERROR;
    int i1 = 0;
    for(int j = 0; j < strlen(num); ++j)
    {
        if(num[j] == '.')
            i1++;
    }
    if(i1 > 1)
        return ERROR;
    for(int i = 0; i < strlen(num); ++i)
    {
        if(num[i] == '.')
            continue;
        if(toNum(num[i]) >= ss0)
            return ERROR;
    }
    return NULL;
}

char* transform(int ss0, int ss1, char* num)
{
    if(checkForSyntax(ss0,ss1,num) != NULL)
        return ERROR;
    if(ss0 == ss1)
        return num;
    unsigned long long div = 0;
    long double fraq = 0;
    toDecimal(ss0, num, &div, &fraq);
    return fromDecimal(ss1, div, fraq);
    //return toDecimal(ss0, num);
}

int main()
{
    char a[100] = {0};
    int ss0,ss1;
    scanf("%d %d", &ss0, &ss1);
    scanf("%s", a);
    char* res = transform(ss0,ss1,a);
    printf("%s\n", res);
    free(res);
    return 0;
}