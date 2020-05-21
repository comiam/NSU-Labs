#include <stdio.h>
#include <malloc.h>

void qsort0(long* a, long begin, long end)
{
    int i = begin, j = end;
    int pivot = a[(begin + end) / 2];
    int tmp;
    while (i <= j)
    {
        while (a[i] < pivot)i++;
        while (a[j] > pivot)j--;
        if (i <= j)
        {
            tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
            i++;j--;
        }
    }
    if (begin < j)
        qsort0(a, begin, j);
    if (i < end)
        qsort0(a, i, end);
}

int main()
{
    long size;
    scanf("%d", &size);
    long *a = (long*)malloc(sizeof(long) * size);

    int sorted = 1;
    for (int i = 0; i < size; ++i)
    {
        scanf("%d", &a[i]);
        if(i >= 1 && a[i] < a[i - 1])
            sorted = 0;
    }
    if(!sorted)
        qsort0(a, 0, size - 1);
    for (int j = 0; j < size; ++j)
        printf("%d ", a[j]);
    free(a);
    return 0;
}