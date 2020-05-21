#include <stdio.h>
#include <string.h>
#include <stdio.h>
#include <malloc.h>

void calc(char* exp);
void calc0(char exp[]);
int isInt(char symbol);
int isOp(char symbol);
int getOp(char symbol);
int getInt(char symbol);
int parseInt(char *exp, int begin, int end);
int checkSyntax(char* exp);
int getPriority(int op);
struct stackItem* toRPN(char* exp);
void strcopy(char* src, char* dest, int pos, int k);
void strOpt(char* exp);

struct stackItem
{
    int isOp;
    int value;
};

int main()
{
    char exp[1024];
    fgets(exp, 1024, stdin);
    calc(exp);
    return 0;
}

void calc(char* exp)
{
    if(checkSyntax(exp) == 0)
    {
        printf("Syntax error!");
        return;
    }else if(checkSyntax(exp) == 2)
    {
        printf("Division by zero!");
        return;
    }
    calc0(exp);
}

int checkSyntax(char exp[])
{
    int brackets0 = 0;
    int brackets1 = 0;
    int breakInt = 0;
    int signCount = 0;
    int lastSign = -1;
    int numCount = 0;

    int haveLast = -1; //-1 - nothing 0 - int 1 - op 2 - bracket
    for(int i = 0; i < strlen(exp); i++)
    {
        if(isInt(exp[i]))
        {
            if(haveLast == 0)
                return 0;
            breakInt = 0;
            for(int j = i; j < strlen(exp); j++)
            {
                if(isInt(exp[j]) == 0)
                {
                    breakInt = j - 1;
                    if(haveLast == 1 && lastSign == 3 && parseInt(exp, i, j - 1) == 0)
                        return 2;
                    break;
                } else breakInt = j;
            }
            i = breakInt;
            haveLast = 0;
            signCount = 0;
            numCount++;
        }else if(isOp(exp[i]))
        {
            if(i == strlen(exp) - 1)
                return 0;
            if(signCount >= 1 && numCount == 0)
                return 0;
            if(haveLast == 1 && signCount >= 2)
                return 0;
            if(haveLast == 1 && signCount == 1 && getOp(exp[i]) != 1)
                return 0;
            if(haveLast == 1 && signCount == 1 && getOp(exp[i]) == 1 && isInt(exp[i+1]) == 0)
                return 0;
            lastSign = getOp(exp[i]);
            haveLast = 1;
            signCount++;
        }else
            switch(exp[i])
            {
                case 32:
                    break;
                case '\n':
                    break;
                case '\0':
                    break;
                case '(':
                    brackets0++;
                    haveLast = 2;
                    break;
                case ')':
                    if(haveLast == 2)
                        return 0;
                    brackets1++;
                    break;
                default:
                    return 0;
            }
    }
    strOpt(exp);
    if(haveLast == 1 || haveLast == 2)
        return 0;
    if(haveLast == -1)
        return 0;
    if(brackets0 == brackets1)
        return 1;
    else
        return 0;
}

struct stackItem* toRPN(char* exp)
{
    struct stackItem* RPN = (struct stackItem*) malloc(sizeof(struct stackItem) * strlen(exp));
    struct stackItem opStack[1024];
    memset(RPN, 0, strlen(exp));

    //main-----------
    int sign = 1;
    int indexStack0 = 0;
    int indexStack1 = 0;
    //---------------

    //tmp
    int secondSign = 0;
    //---------------
    for(int i = 0; i < strlen(exp); i++)
    {
        if(exp[i] == 32)
            continue;
        if(isInt(exp[i]) == 1)
        {
            for(int j = i; j < strlen(exp); j++)
            {
                if(isInt(exp[j]) == 0)
                {
                    struct stackItem item = {0,sign * parseInt(exp, i, j - 1)};
                    RPN[indexStack0++] = item;
                    sign = 1;
                    secondSign = 0;
                    i = j - 1;
                    break;
                }else if(j == strlen(exp) - 1)
                {
                    struct stackItem item = {0,sign * parseInt(exp, i, j)};
                    RPN[indexStack0++] = item;
                    sign = 1;
                    secondSign = 0;
                    i = j;
                    break;
                }
            }
        } else if(isOp(exp[i]) == 1)
        {
            if(getOp(exp[i]) == 1 && (i == 0 || (secondSign >= 1 && i < strlen(exp) - 1 && isInt(exp[i + 1]))))
            {
                sign = -1;
                continue;
            }else
                secondSign = 1;
            if(indexStack1 == 0)
            {
                struct stackItem item = {1, getOp(exp[i])};
                opStack[indexStack1++] = item;
                continue;
            }
            struct stackItem item = {1, getOp(exp[i])};
            opStack[indexStack1++] = item;
            for(int j = indexStack1 - 2; j >= 0; j--)
            {
                if(getPriority(opStack[indexStack1 - 1].value) <= getPriority(opStack[j].value))
                {
                    RPN[indexStack0++] = opStack[j];
                    indexStack1--;

                    for(int k = j; k < 1023; k++)
                    {
                        opStack[k] = opStack[k + 1];
                    }
                }
            }
        }else if(exp[i] == 40)
        {
            int opBr = 1;
            for(int j = i + 1; j < strlen(exp); ++j)
            {
                if(exp[j] == 40)
                    opBr++;
                if(exp[j] == 41 && opBr == 1)
                {
                    char br[j - i];
                    strcopy(exp, br, i + 1, j - 1);
                    br[j - i] = '\0';
                    struct stackItem* rpn = toRPN(br);
                    for(int k = 0; k < 1024; ++k)
                    {
                        if(rpn[k].isOp == -1)
                            break;
                        RPN[indexStack0++] = rpn[k];
                    }


                    free(rpn);
                    i = j;
                    secondSign = 0;
                    break;
                } else if(exp[j] == 41)
                    opBr--;
            }
        }
    }
    for(int i = indexStack1 - 1; i >= 0; i--)
        RPN[indexStack0++] = opStack[i];
    struct stackItem item = {-1, 0};
    RPN[indexStack0] = item;
    return RPN;
}

void calc0(char* exp)
{
    int stackIndex = 0;
    double numStack[1024];
    struct stackItem* stack = toRPN(exp);
    for(int i = 0; i < 1024; i++)
    {
        if(stack[i].isOp == -1)
            break;
        if(stack[i].isOp == 0)
        {
            numStack[stackIndex++] = stack[i].value;
            continue;
        }else
        {
            switch(stack[i].value)
            {
                case 0:
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] + numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case 1:
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] - numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case 2:
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] * numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case 3:
                    if(numStack[stackIndex - 1] == 0)
                    {
                        printf("Division by zero!");
                        free(stack);
                        return;
                    }
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] / numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                default:
                    printf("Error :c");
                    free(stack);
                    return;
            }
            continue;
        }
    }
    free(stack);
    if(numStack[0] - (int)numStack[0] < 0.001)
        printf("%d\n", (int)numStack[0]);
    else
        printf("%.3lf\n", numStack[0]);
}

int getInt(char symbol)
{
    return symbol - '0';
}

int parseInt(char *exp, int begin, int end)
{
    int result = 0;
    int exponent = 1;
    for(int i = end; i >= begin; --i)
    {
        result += getInt(exp[i])*exponent;
        exponent *= 10;
    }
    return result;
}

int getOp(char symbol)
{
    if(symbol == 43)//+ 0
        return 0;
    else if(symbol == 45)//- 1
        return 1;
    else if(symbol == 42)//* 2
        return 2;
    else if(symbol == 47)// / 3
        return 3;
    else if(symbol == 40)// ( 4
        return 4;
    else if(symbol == 41)// ) 4
        return 4;
    return -1;
}

int getPriority(int op)
{
    switch(op)
    {
        case 0:
            return 1;
        case 1:
            return 1;
        case 2:
            return 2;
        case 3:
            return 2;
        case 4:
            return 0;
        default:
            return -1;
    }
}

void strcopy(char* src, char* dest, int pos, int k)
{
    if (pos > strlen(src))
    {
        dest[0] = 0;
        return;
    }
    int i = 0;
    for (; i < k; ++i)
    {
        dest[i] = src[pos++];
    }
    dest[i] = 0;
}

int isOp(char symbol)
{
    if(symbol == 45 || symbol == 43 || symbol == 42 || symbol == 47)
        return 1;
    return 0;
}

int isInt(char symbol)
{
    if(symbol >= 48 && symbol <= 57)
        return 1;
    else return 0;
}

void strOpt(char* exp)
{
    for (int i = 0; i < strlen(exp); ++i)
    {
        if(isOp(exp[i]) == 1 && getOp(exp[i]) > 1)
            return;
    }
    for (int j = 0; j < strlen(exp); ++j)
    {
        if(exp[j] == 40 || exp[j] == 41)
            exp[j] = 32;
    }
}