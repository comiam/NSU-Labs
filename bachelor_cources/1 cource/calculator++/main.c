#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>
#include <ctype.h>

#define DEFAULT_EXPRESSION_SIZE 1024
#define SYNTAX_ERROR -10
#define END_RPN -1
#define PLUS 0
#define MINUS 1
#define MUL 2
#define DIV 3
#define POW 4
#define LBRACKET 5
#define RBRACKET 6

typedef struct stackItem
{
    int isOp;
    long value;
} StackItem;

char* endPtr;
double lastAns = 0;
int mode = 0;
int HUSTON_I_HAVE_A_PROBLEM = 0;

int isSymbol(char c);
void removeSpaces(char* exp);
void printAnswer(double out, int value);
void syntaxError(StackItem *RPN, int indexStack0);
double calc(char* exp);
int isNum(char symbol);
long getNum(char *exp);
int isOp(char symbol);
int getOp(char symbol);
int getOpPriority(int op);
StackItem* toRPN(char* exp);
StackItem* getNItem(char* exp);

int main(int argc, char* argv[])
{
    if(argc == 1)
    {
        char exp[DEFAULT_EXPRESSION_SIZE];
        if(fgets(exp, DEFAULT_EXPRESSION_SIZE, stdin) == NULL)
        {
            printf("Bad input!\n");
            return 0;
        }
        removeSpaces(exp);
        double ans = calc(exp);
        if(!HUSTON_I_HAVE_A_PROBLEM)
            printAnswer(ans, 0);
    }else if(argc == 2 && !strcmp(argv[1], "-i"))
    {
        mode = 1;
        char exp[DEFAULT_EXPRESSION_SIZE];
        double ans = 0;
        do
        {
            printf("in> ");
            if(fgets(exp, DEFAULT_EXPRESSION_SIZE, stdin) == NULL)
            {
                printf("Bad input!\n");
                continue;
            }
            removeSpaces(exp);
            if(!strcmp(exp,"exit"))
                break;
            ans = calc(exp);
            if(!HUSTON_I_HAVE_A_PROBLEM)
                printAnswer(ans, 1);
            lastAns = ans;
        }while(1);
    }else printf("Wrong arguments!\n");
    return 0;
}

void removeSpaces(char* exp)
{
    for (int i = 0; i < strlen(exp); ++i)
        if(exp[i] == '\n')
            exp[i] = '\0';
    while(exp[0] == ' ')
        for (int i = 1; i < strlen(exp); ++i)
            exp[i - 1] = exp[i];
    while(exp[strlen(exp) - 1] == ' ')
        exp[strlen(exp) - 1] = '\0';
}

void printAnswer(double out, int interactive)
{
    if(out - (int)out < 0.001)
        printf(interactive == 1 ? "out< %d\n" : "%d\n", (int)out);
    else
        printf(interactive == 1 ? "out< %.3lf\n" : "%.3lf\n", out);
}

double calc(char* exp)
{
    int stackIndex = 0;
    double numStack[DEFAULT_EXPRESSION_SIZE];

    StackItem* RPN = toRPN(exp);

    //int f = 0;
   // while(RPN[f].isOp != END_RPN)
   // {
   //     printf("%d ", RPN[f++].value);
   // }

    if(RPN[0].isOp == SYNTAX_ERROR || RPN[0].isOp == END_RPN)
    {
        printf("Syntax error!\n");
        free(RPN);
        HUSTON_I_HAVE_A_PROBLEM = 1;
        return 0;
    }

    for(int i = 0; i < DEFAULT_EXPRESSION_SIZE; i++)
    {
        if(RPN[i].isOp == END_RPN)
            break;
        if(RPN[i].isOp == 0)
        {
            numStack[stackIndex++] = RPN[i].value;
            continue;
        }else
        {
            switch(RPN[i].value)
            {
                case PLUS:  // PLUS
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] + numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case MINUS:
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] - numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case MUL:
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] * numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case DIV:
                    if(numStack[stackIndex - 1] == 0)
                    {
                        printf("Division by zero!\n");
                        free(RPN);
                        HUSTON_I_HAVE_A_PROBLEM = 1;
                        return 0;
                    }
                    numStack[stackIndex - 2] = numStack[stackIndex - 2] / numStack[stackIndex - 1];
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                case POW:
                    if(numStack[stackIndex - 1] == 0)
                    {
                        numStack[stackIndex - 2] = 1;
                        break;
                    }
                    double old = numStack[stackIndex - 2];
                    for (int j = 0; j < numStack[stackIndex - 1] - 1; ++j)
                    {
                        numStack[stackIndex - 2] *= old;
                    }
                    numStack[stackIndex - 1] = 0;
                    stackIndex--;
                    break;
                default:
                    printf("Error :c . ID %d\n", RPN[i].isOp);
                    free(RPN);
                    HUSTON_I_HAVE_A_PROBLEM = 1;
                    return 0;
            }
            continue;
        }
    }

    free(RPN);
    HUSTON_I_HAVE_A_PROBLEM = 0;
    return numStack[0];
}

StackItem* toRPN(char* exp)
{
    StackItem* RPN = (StackItem*)malloc(sizeof(StackItem) * DEFAULT_EXPRESSION_SIZE);
    StackItem opStack[DEFAULT_EXPRESSION_SIZE];
    memset(RPN, 0, DEFAULT_EXPRESSION_SIZE * sizeof(StackItem));

    int secondSign = 0;
    int isOpened = 0;
    int indexStack0 = 0;
    int indexStack1 = 0;
    char last_answer_symbol = '\0';

    const size_t strSize = strlen(exp);

    for(int i = 0; i < strSize; i++)
    {
        if(exp[i] == 32)
            continue;
        else if(isNum(exp[i]))
        {
            if(indexStack0 > 0 && indexStack1 == 0)
            {
                syntaxError(RPN, indexStack0);
                return RPN;
            }
            StackItem* item = getNItem(&(exp[i]));
            RPN[indexStack0++] = *item;
            free(item);
            i = (int)(endPtr - exp - 1);
            secondSign = 0;
            continue;
        } else if(isOp(exp[i]))
        {
            if(getOp(exp[i]) == MINUS && ((i == 0 && i < strSize - 1 && isNum(exp[i + 1])) || (secondSign >= 1 && i < strSize - 1 &&
                    isNum(exp[i + 1])))) //Нахождение отрицательного числа
            {
                StackItem* item = getNItem(&(exp[i]));
                RPN[indexStack0++] = *item;
                free(item);
                i = (int)(endPtr - exp - 1);
                secondSign = 0;
                continue;
            } else if(i == strSize - 1 || //конец строки
                    (secondSign > 0 && getOp(exp[i]) != MINUS) || //два занка подряд
                    (i == 0 && i < strSize - 1 && !isNum(exp[i + 1]) && !isSymbol(exp[i + 1])) || //проверка условия отрицательного числа в начале выражения
                    ((getOp(exp[i]) == MINUS && !isNum(exp[i + 1]) &&
                    getOp(exp[i + 1]) != LBRACKET && exp[i + 1] != 32) && (mode != 1 || !isSymbol(exp[i + 1])))) //проверка числа после знака -
            {
                syntaxError(RPN, indexStack0);
                return RPN;
            } else
                secondSign = 1;

            StackItem item = {1, getOp(exp[i])};
            opStack[indexStack1++] = item;

            if(indexStack1 == 1)
                continue;

            for(int j = indexStack1 - 2; j >= 0; j--)
                if(getOpPriority(opStack[indexStack1 - 1].value) <= getOpPriority(opStack[j].value) && opStack[j].value != LBRACKET)
                {
                    if(j == indexStack1 - 2 && opStack[indexStack1 - 1].value == POW && opStack[j].value == POW)
                        continue;
                    RPN[indexStack0++] = opStack[j];
                    indexStack1--;
                    for(int k = j; k < indexStack1; k++)
                        opStack[k] = opStack[k + 1];
                }else if(opStack[j].value == LBRACKET)
                    break;

        } else if(getOp(exp[i]) == LBRACKET)
        {
            StackItem item = {1, getOp(exp[i])};
            opStack[indexStack1++] = item;
            isOpened++;
        } else if(getOp(exp[i]) == RBRACKET && isOpened)
        {
            if(i > 0 && getOp(exp[i - 1]) == LBRACKET)
            {
                syntaxError(RPN, indexStack0);
                return RPN;
            }
            while(opStack[indexStack1 - 1].value != LBRACKET)
            {
                RPN[indexStack0++] = opStack[--indexStack1];
                if(indexStack1 == 0)
                {
                    syntaxError(RPN, indexStack0);
                    return RPN;
                }
            }
            indexStack1--;
            isOpened--;
        } else if(getOp(exp[i]) == RBRACKET && !isOpened)
        {
            syntaxError(RPN, indexStack0);
            return RPN;
        }else if(isSymbol(exp[i]) && mode == 1)
        {
            if(last_answer_symbol != '\0' && last_answer_symbol != exp[i])
            {
                syntaxError(RPN, indexStack0);
                return RPN;
            }else if(last_answer_symbol != '\0' && last_answer_symbol == exp[i])
            {
                StackItem item = {0, (long)lastAns};
                RPN[indexStack0++] = item;
                secondSign = 0;
                continue;
            }else if(last_answer_symbol == '\0' &&
                ((i == 0) ||
                (i > 0 && i < strSize - 1 && !isSymbol(exp[i - 1]) && !isSymbol(exp[i + 1])) ||
                (i == strSize - 1 && !isSymbol(exp[i - 1]))))
            {
                last_answer_symbol = exp[i];
                StackItem item = {0, (long)lastAns};
                RPN[indexStack0++] = item;
                secondSign = 0;
                continue;
            }
        }else {
            syntaxError(RPN, indexStack0);
            return RPN;
        }
    }

    for(int i = indexStack1 - 1; i >= 0; i--)
        RPN[indexStack0++] = opStack[i];

    if(isOpened)
    {
        syntaxError(RPN, indexStack0);
        return RPN;
    }
    StackItem item = {END_RPN, 0};
    RPN[indexStack0] = item;

    return RPN;
}

StackItem* getNItem(char* exp)
{
    StackItem* item = (StackItem*)malloc(sizeof(StackItem));
    item->isOp = 0;
    item->value = getNum(exp);
    return item;
}

void syntaxError(StackItem *RPN, int indexStack0)
{
    if(indexStack0 > 0)
        RPN[0].isOp = SYNTAX_ERROR;
    else
    {
        StackItem item = {SYNTAX_ERROR, 0};
        RPN[0] = item;
    }
}

long getNum(char *exp)
{
    return strtol(exp, &endPtr, 10);
}

int getOp(char symbol)
{
    if(symbol == 43)//+ 0
        return PLUS;
    else if(symbol == 45)//- 1
        return MINUS;
    else if(symbol == 42)//* 2
        return MUL;
    else if(symbol == 47)// / 3
        return DIV;
    else if(symbol == 94)// ^ 4
        return POW;
    else if(symbol == 40)// ( 5
        return LBRACKET;
    else if(symbol == 41)// ) 6
        return RBRACKET;
    return -1;
}

int getOpPriority(int op)
{
    switch(op)
    {
        case PLUS:
            return 1;
        case MINUS:
            return 1;
        case MUL:
            return 2;
        case DIV:
            return 2;
        case POW:
            return 3;
        case LBRACKET:
            return 0;
        case RBRACKET:
            return 0;
        default:
            return -1;
    }
}

int isOp(char symbol)
{
    if(symbol == 45 || symbol == 43 || symbol == 42 || symbol == 47 || symbol == 94)
        return 1;
    return 0;
}

int isNum(char symbol)
{
    if(symbol >= 48 && symbol <= 57)
        return 1;
    else return 0;
}
int isSymbol(char c)
{
    if(isalpha(c))
        return 1;
    else switch(c)
    {
        case '@':
            return 1;
        case '#':
            return 1;
        case '$':
            return 1;
        case '&':
            return 1;
        case '%':
            return 1;
        default:
            return 0;
    }
}
