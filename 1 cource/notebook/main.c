#include <stdio.h>
#include <malloc.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>
#include <windows.h>
#include <locale.h>

#define MAX_STRING_LENGTH 5000
#define END -99

typedef struct ListItem {
    struct ListItem* next;
    long index;
    char* date;
    int rate;
    unsigned char feelings[MAX_STRING_LENGTH];

} Item;

typedef struct LinkedList {
    Item* first;
    long size;
} LinkedList;

enum filterType {
    date,
    day,
    month,
    year,
    rate,
    feelings
};

//Base-functions------------------------------------------------
LinkedList* createList(long size);
int loadListFrom(char* file, LinkedList** list);
int saveListTo(char* file, LinkedList* list);
void printListTo(FILE* file, LinkedList* list);
void printList(LinkedList* list);
void printItems(Item* list);
void removeItems(LinkedList* list, Item* listI);
void removeList(LinkedList* list);
void removeItem(LinkedList* list, long index);
Item* addItem(LinkedList* list);
Item* getItem(LinkedList* list, long index);
//Interface-functions-------------------------------------------
int addNote(LinkedList* list);
void showNotes(LinkedList* list);
Item* filter0(LinkedList* list, enum filterType type, char* arg);
void filter(LinkedList* list, Item** stack);
int save(LinkedList* list, char* file);
void load(LinkedList** list, char* file);
void continueProgram();
//interface-utilities-------------------------------------------
int* getInts(char* date);
int isDate(char* date);
void removeSpaces(unsigned char* exp);
int rabinKarp(unsigned char *s, unsigned char *sub);

int main()
{
	setlocale(LC_ALL, "Russian");
    SetConsoleCP(1251);
    SetConsoleOutputCP(1251);
    printf("Добро подаловать в дневник V1.0 Beta!\nВы можете использовать следующие команды:\n"
           "1) Добавить новую запись: add-new\n"
           "2) Показать все записи: show\n"
           "3) Фильтровать записи по дате, оценке дня, описанию: filter {--date, --day, --month, --year, --rate, --feel-word} <arg>\n"
           "4) Удалить записи по фильтру: delete {--date, --day, --month, --year, --rate, --feel-word} <arg>\n"
           "6) Сохранить ваш дневник: save <directory to file.txt>(мы ненавидим кириллицу в директории, сорян)\n"
           "7) Загрузить ваш дневник для редактирования: load <directory to file>(мы ненавидим кириллицу в директории, сорян)\n"
           "8) Выйти из программы с сохранением: exit\n"
           "\n"
           "Введите команду: ");

    LinkedList* list = createList(0);
    char buffer[MAX_STRING_LENGTH];
    int saved = 1;

    while(1)
    {
        scanf("%s", buffer);

        if(!strcmp(buffer, "add-new"))
        {
            if(addNote(list))
            {
                printf("Запись успешно добавлена!\n");
                saved = 0;
            }
            else
                printf("Отменено!\n");

        }else if(!strcmp(buffer, "show"))
        {
            printf("Вот весь дневник:\n");
            showNotes(list);

        }else if(!strcmp(buffer, "filter"))
        {
            Item* stack = NULL;
            filter(list, &stack);

            if(stack != NULL)
            {
                printf("Вот все найденное:\n");
                printItems(stack);
            }else
                printf("Ничего не найдено.\n");

        }else if(!strcmp(buffer, "delete"))
        {
            Item* stack = NULL;
            filter(list, &stack);

            if(stack != NULL)
            {
                printf("Все перечисленное будет удалено:\n");
                printItems(stack);
                repeat:
                printf("Удалить эти записи? y/n\n");
                scanf("%s", buffer);
                if(!strcmp("y", buffer))
                    removeItems(list, stack);
                else if(!strcmp("n", buffer));
                else
                    goto repeat;
            }else
                printf("Ничего не найдено.\n");

        }else if(!strcmp(buffer, "save"))
        {
            printf("Введите директорию: ");
            scanf("%s", buffer);
            if(save(list, buffer))
                saved = 1;

        }else if(!strcmp(buffer, "load"))
        {
            printf("Введите директорию: ");
            scanf("%s", buffer);
            load(&list, buffer);
        }else if(!strcmp(buffer, "exit"))
        {
            if(saved)
            {
                printf("Дневник уже сохранен.");
                break;
            }
            printf("Введите директорию: ");
            scanf("%s", buffer);
            if(save(list, buffer))
                break;
            else
            {
                repeat1:
                printf("Все равно закрыть программу? y/n\n");
                scanf("%s", buffer);
                if(!strcmp("y", buffer))
                    break;
                else if(!strcmp("n", buffer))
                    continue;
                else
                    goto repeat1;
            }
        }
        continueProgram();
    }
    removeList(list);
    return 0;
}

//Begin-of-implementation-interface-functions

void continueProgram()
{
    printf("Введите команду: ");
}

int addNote(LinkedList* list)
{
    char a[11];
    unsigned char b[MAX_STRING_LENGTH];
    int rate = 0;
    dateR:
    printf("Введите дату dd.mm.yyyy:\n");
    scanf("%s", a);
    if(!strcmp("!not", a))
        return 0;

    if(!isDate(a))
        goto dateR;
    else {
        int* dates = getInts(a);
        if(dates[0] > 31 || dates[0] < 0 ||
                dates[1] > 12 || dates[1] < 0 || dates[2] < 0)
        {
            free(dates);
            goto dateR;
        }
    }
    rateR:
    printf("Введите оценку дня от 0 до 10:\n");
    scanf("%d", &rate);
    if(rate < 0 || rate > 10)
        goto rateR;

    printf("Опиши свой день:\n");
    fflush(stdin);
    //fread(b, sizeof(char), MAX_STRING_LENGTH, stdin);
    fgets(b, MAX_STRING_LENGTH, stdin);
	removeSpaces(b);
    //scanf("");

    //printf("%d %s", (int)strlen(b), b);
    if(!strcmp("!not", (char*)b))
        return 0;

    Item* i = addItem(list);
    memcpy(i->date, a, 11 * sizeof(char));
    i->rate = rate;
    memcpy(i->feelings, b, MAX_STRING_LENGTH * sizeof(char));
    return 1;
}

void showNotes(LinkedList* list)
{
    printList(list);
}

void load(LinkedList** list, char* file)
{
    if(loadListFrom(file, list))
        printf("Загрузка прошла успешно!\n");
    else
        printf("Произошла ошибка при загрузке:(\n");

}

int save(LinkedList* list, char* file)
{
    if(saveListTo(file, list))
    {
        printf("Дневник успешно сохранен!\n");
        return 1;
    }
    else
    {
        printf("Произошла ошибка при сохранении:(\n");
        return 0;
    }
}

void filter(LinkedList* list, Item** stack)
{
    char buffer[MAX_STRING_LENGTH];
    int bufferI = 0;

    scanf("%s", buffer);

    if(!strcmp(buffer, "--date"))
    {
        scanf("%s", buffer);
        int success = 1;
        if(!isDate(buffer))
            success = 0;
        else {
            int* dates = getInts(buffer);
            if(dates[0] > 31 || dates[0] < 0 ||
               dates[1] > 12 || dates[1] < 0 || dates[2] < 0)
            {
                free(dates);
                success = 0;
            }
        }
        if(!success)
        {
            printf("Ошибка в 2 аргументе!\n");
            return;
        }
        *stack = filter0(list, date, buffer);
    }else if(!strcmp(buffer, "--day"))
    {
        scanf("%i", &bufferI);
        if(bufferI > 31 || bufferI < 0)
        {
            printf("Ошибка в 2 аргументе!\n");
            return;
        }
        *stack = filter0(list, day, itoa(bufferI, buffer, 10));
    }else if(!strcmp(buffer, "--month"))
    {
        scanf("%i", &bufferI);
        if(bufferI > 12 || bufferI < 0)
        {
            printf("Ошибка в 2 аргументе!\n");
            return;
        }
        *stack = filter0(list, month, itoa(bufferI, buffer, 10));
    }else if(!strcmp(buffer, "--year"))
    {
        scanf("%i", &bufferI);

        if(bufferI < 0)
        {
            printf("Ошибка в 2 аргументе!\n");
            return;
        }
        *stack = filter0(list, year, itoa(bufferI, buffer, 10));
    }else if(!strcmp(buffer, "--rate"))
    {
        scanf("%i", &bufferI);
        if(bufferI > 10 || bufferI < 0)
        {
            printf("Ошибка в 2 аргументе!\n");
            return;
        }
        *stack =  filter0(list, rate, itoa(bufferI, buffer, 10));
    }else if(!strcmp(buffer, "--feel-word"))
    {
		unsigned char buf2[MAX_STRING_LENGTH];
		fgets(buf2, MAX_STRING_LENGTH, stdin);
		removeSpaces(buf2);

        *stack = filter0(list, feelings, buf2);
    }else
        printf("Ошибка в 1 аргументе!\n");
}

Item* filter0(LinkedList* list, enum filterType type, char* arg)
{
    int size = 0;
    Item* listI = realloc(NULL, 0);
    switch(type)
    {
        case date:
        {
            for (int i = 0; i < list->size; ++i)
            {
                if(!strcmp(getItem(list, i)->date, arg))
                {
                    listI = realloc(listI, (++size) * sizeof(Item));
                    listI[size - 1] = *getItem(list, i);
                }
            }
            break;
        }
        case day:
        {
            int day = atoi(arg);
            for (int i = 0; i < list->size; ++i)
            {
                int* a = getInts(getItem(list, i)->date);
                if(a[0] == day)
                {
                    listI = realloc(listI, (++size) * sizeof(Item));
                    listI[size - 1] = *getItem(list, i);
                }
                free(a);
            }
            break;
        }
        case month:
        {
            int month = atoi(arg);
            for (int i = 0; i < list->size; ++i)
            {
                int* a = getInts(getItem(list, i)->date);
                if(a[1] == month)
                {
                    listI = realloc(listI, (++size) * sizeof(Item));
                    listI[size - 1] = *getItem(list, i);
                }
                free(a);
            }
            break;
        }
        case year:
        {
            int year = atoi(arg);
            for (int i = 0; i < list->size; ++i)
            {
                int* a = getInts(getItem(list, i)->date);
                if(a[2] == year)
                {
                    listI = realloc(listI, (++size) * sizeof(Item));
                    listI[size - 1] = *getItem(list, i);
                }
                free(a);
            }
            break;
        }
        case rate:
        {
            int rate = atoi(arg);
            for (int i = 0; i < list->size; ++i)
            {
                if(getItem(list, i)->rate == rate)
                {
                    listI = realloc(listI, (++size) * sizeof(Item));
                    listI[size - 1] = *getItem(list, i);
                }
            }
            break;
        }
        case feelings:
        {
            for (int i = 0; i < list->size; ++i)
            {
                if(rabinKarp(getItem(list, i)->feelings, (unsigned char*)arg))
                {
                    listI = realloc(listI, (++size) * sizeof(Item));
                    listI[size - 1] = *getItem(list, i);
                }
            }
            break;
        }
    }
    if(size > 0)
    {
        listI = realloc(listI, (++size) * sizeof(Item));
        listI[size - 1].index = END;
        return listI;
    }else
        return NULL;
}

//Begin-of-implementation-base-functions

LinkedList* createList(long size)
{
    LinkedList* list = (LinkedList*)malloc(sizeof(LinkedList));
    Item* last = NULL;
    list->size = size;
    list->first = NULL;

    for (int i = 0; i < size; ++i)
    {
        Item* item = (Item*)malloc(sizeof(Item));
        item->index = i;
        item->date = (char*)malloc(11 * sizeof(char));
        item->rate = 0;
        memset(item->feelings, 0, MAX_STRING_LENGTH * sizeof(char));
        if(last != NULL)
            last->next = item;
        else
            list->first = item;
        last = item;
        if(i == size - 1)
            item->next = NULL;
    }
    return list;
}

Item* getItem(LinkedList* list, long index)
{
    Item* last = NULL;
    int was = 0;
    while(1)
    {
        if(last == NULL && !was)
        {
            last = list->first;
            was = 1;
        }
        if(last == NULL)
            return NULL;

        if(last->index == index)
            return last;
        else
            last = last->next;
    }
}

Item* addItem(LinkedList* list)
{
    Item* last  = list->first;
    Item* last1 = NULL;
    Item* item  = (Item*)malloc(sizeof(Item));
    item->index = 0;
    item->date  = (char*)malloc(11 * sizeof(char));
    item->rate  = 0;
    item->next  = NULL;
    memset(item->feelings, 0, MAX_STRING_LENGTH * sizeof(char));
    while(1)
    {
        if(last == NULL)
        {
            if(last1 != NULL)
            {
                last1->next = item;
                item->index = last1->index + 1;
            }else
            {
                list->first = item;
                item->index = 0;
            }

            list->size++;
            break;
        }

        last1 = last;
        last = last->next;
    }
    return item;
}

void removeItem(LinkedList* list, long index)
{
    Item* last = NULL;
    Item* last1 = NULL;
    while(1)
    {
        if(last == NULL)
            last = list->first;
        if(last->index == index)
        {
            if(index != list->size - 1)
                for (int i = index + 1; i < list->size; ++i)
                    getItem(list, i)->index--;

            if(last == list->first)
                list->first = last->next;
            else
                last1->next = last->next;

            free(last);
            list->size--;
            break;
        }else
        {
            last1 = last;
            last = last->next;
        }
    }
}

void removeList(LinkedList* list)
{
    for (int i = list->size - 1; i >= 0; --i)
        removeItem(list, i);
    free(list);
}

void removeItems(LinkedList* list, Item* listI)
{
    int ind = 0;
    Item it;
    while((it = listI[ind++]).index != END)
        removeItem(list, it.index - (ind - 1));

    free(listI);
}

void printItems(Item* list)
{
    int ind = 0;
    Item it;
    printf("\\\\--------------------------------------------\n");
    while((it = list[ind++]).index != END)
        printf("Дата: %s\nНастроение: %d\nЧто было: %s\n\\\\--------------------------------------------\n",
               it.date, it.rate, it.feelings);

}

void printList(LinkedList* list)
{
    Item* item = NULL;
    printf("\\\\--------------------------------------------\n");
    for (int i = 0; i < list->size; ++i)
    {
        item = getItem(list, i);
        printf("Дата: %s\nНастроение: %d\nЧто было: %s\n\\\\--------------------------------------------\n",
               item->date, item->rate, item->feelings);
    }
}

void printListTo(FILE* file, LinkedList* list)
{
    Item* item = NULL;
    for (int i = 0; i < list->size; ++i)
    {
        item = getItem(list, i);
        fprintf(file, "d: %s\nr: %d\nf: %s\n",
                item->date, item->rate, item->feelings);
    }
}

int saveListTo(char* file, LinkedList* list)
{
    FILE* out = fopen(file, "w");

    if(out == NULL)
        return 0;
    fprintf(out, "%ld\n", list->size);
    printListTo(out, list);
    fclose(out);
    return 1;
}

int loadListFrom(char* file, LinkedList** list)
{
    FILE* in = fopen(file, "r");
    if(in == NULL)
        return 0;

    long count = 0;

    fscanf(in, "%ld", &count);

    LinkedList old = **list;
    //LinkedList* newL = createList(count);
    *list = createList(count);
    //free(newL);
    char tmp[11] = {' '};
    unsigned char  tmpL[MAX_STRING_LENGTH];
    Item* item = NULL;
    int tmpD = 0;

    for (int i = 0; i < count; ++i)
    {
        item = getItem(*list, i);

        fscanf(in, "%s", tmp);
        fscanf(in, "%s", tmp);
        if(!isDate(tmp))
        {
            removeList(*list);
            (**list) = old;
            return 0;
        }
        strcpy(item->date, tmp);

        fscanf(in, "%s", tmp);
        fscanf(in, "%d", &tmpD);
        if(tmpD < 0 || tmpD > 10)
        {
            removeList(*list);
            (**list) = old;
            return 0;
        }
        item->rate = tmpD;

        fscanf(in, "%s", tmp);
        fgets((unsigned char*)tmpL, MAX_STRING_LENGTH, in);
		removeSpaces(tmpL);
		
        memcpy(item->feelings, tmpL, MAX_STRING_LENGTH * sizeof(char));
    }
    return 1;
}

//begin of interface utilities
int* getInts(char* date)
{
    char* cpy = (char*)malloc(strlen(date) * sizeof(char));
    strcpy(cpy, date);
    char * pch = strtok (cpy,".");
    int* array = (int*)malloc(3 * sizeof(int));
    int k = 0;
    while (pch != NULL)
    {
        array[k++] = atoi(pch);
        pch = strtok (NULL, ".");
    }
    free(cpy);
    return array;
}

int isDate(char* date)
{
    if(strlen(date) == 10 && isdigit(date[0]) && isdigit(date[1]) && date[2] == '.' &&
            isdigit(date[3]) && isdigit(date[4]) && date[5] == '.' &&
            isdigit(date[6]) && isdigit(date[7]) && isdigit(date[8]) && isdigit(date[9]))
        return 1;
    return 0;
}

int rabinKarp(unsigned char *s, unsigned char *sub)
{
    int pattern_hash = 0;
    int text_hash = 0;
    int chtr = 0;

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
            if (chtr)
                return 1;
        }

        if (i < strlen(s) - strlen(sub))
        {
            text_hash -= (s[i] % 3);
            text_hash -= (text_hash / 3) * 2;
            text_hash += (int) ((s[i + strlen(sub)] % 3) * pow(3, strlen(sub) - 1));
        }
    }
    return 0;
}

void removeSpaces(unsigned char* exp)
{
    for (int i = 0; i < strlen(exp); ++i)
        if(exp[i] == (unsigned char)'\n' || exp[i] == (unsigned char)'\r')
            exp[i] = (unsigned char)'\0';
		
    while(exp[0] == ' ')
        for (int i = 1; i < strlen(exp); ++i)
		{
			exp[i - 1] = exp[i];
			exp[i] = (unsigned char)' ';
		}
            
		
    while(exp[strlen(exp) - 1] == (unsigned char)' ')
        exp[strlen(exp) - 1] = (unsigned char)'\0';
}