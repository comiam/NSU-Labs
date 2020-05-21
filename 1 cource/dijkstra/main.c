#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <string.h>

#define NO_PATH -2
#define OVERFLOW -1
#define INFINITY -5
#define INT_MAX_ABSTRACT -4
#define MAY_CHANGE 1
#define NOT_CHANGE 0
#define CROSSED 1

int notCrossedCount(int* crossed, int vertices);
int isCrossed(int index, int* table);
int sum(int a, int b);
int checkGreatest(int a, int b);
int pathExist(int begin, int end, int** graph);
int checkRange(int begin, int end, int value);
int checkEdgeCapacity(int old, int new);
int* dijkstra(int begin, int end, int** graph, int vertices);
void selectNext(int* crossed, const int* graph, int* end, int vertices);

int main()
{
    int vertices = 0;
    int B = 0, E = 0;
    int edges = 0;
    scanf("%d", &vertices);
    scanf("%d %d", &B, &E);
    scanf("%d", &edges);

    if(!checkRange(0, vertices, B) || !checkRange(0, vertices, E) || vertices == 0)
    {
        printf("bad vertex\n");
        return 0;
    }
    if(!checkRange(0, 5000, vertices))
    {
        printf("bad number of vertices\n");
        return 0;
    }
    if(!checkRange(0, vertices*(vertices + 1) / 2, edges))
    {
        printf("bad number of edges\n");
        return 0;
    }

    int** graph;
    graph = calloc((size_t)vertices + 1, sizeof(int*));
    for (int i = 0; i < vertices + 1; ++i)
        graph[i] = calloc((size_t)vertices, sizeof(int));

    int B1,E1,weight;
    for (int i = 0; i < edges; i++)
    {
        scanf("%d %d %d", &B1, &E1, &weight);
        if(weight < 0)
        {
            for (int j = 0; j < vertices + 1; ++j)
                free(graph[j]);
            free(graph);
            printf("bad length\n");
            return 0;
        }
        graph[--B1][--E1] = weight;
        graph[E1][B1] = weight;
    }

    int* result = dijkstra(--B, --E, graph, vertices);

    for (int j = 0; j < vertices; ++j)//Печатаем длины кратчайших путей, для этого был зарезервирован массив в переменной graph
    {
        if(graph[vertices][j] == INT_MAX_ABSTRACT)
            printf("INT_MAX+ ");
        else if(graph[vertices][j] == INFINITY || graph[vertices][j] == NO_PATH)
            printf("oo ");
        else
            printf("%i ", graph[vertices][j]);
    }

    printf("\n");

    if(result[0] == OVERFLOW)//Печатаем наш путь, он как раз в result
    {printf("overflow");}
    else if(result[0] == NO_PATH)
    {printf("no path");}
    else for (int k = 0; k < vertices; k++)
        if(result[k] == -1)
            break;
        else
            printf("%i ", result[k] + 1);

    free(result);
    for (int i = 0; i < vertices + 1; ++i)
        free(graph[i]);
    free(graph);

    return 0;
}

int* dijkstra(int begin, int end, int** graph, int vertices)
{
    for (int i = 0; i < vertices; ++i)
        graph[vertices][i] = i == begin ? 0 : INFINITY;

    int* crossedEnds = (int*)malloc(vertices * sizeof(int));
    memset(crossedEnds, 0, vertices * sizeof(int));

    int* path = (int*)malloc(vertices * sizeof(int));
    memset(path, 0, vertices * sizeof(int));

    int* parent = (int*)malloc(vertices * sizeof(int));
    memset(parent, 0, vertices * sizeof(int));

    int indexPath = 0;
    int currentVertex = begin;
    int tmp;

    for(; 0 <= notCrossedCount(crossedEnds, vertices); selectNext(crossedEnds, graph[vertices], &currentVertex, vertices))
    {
        if(currentVertex == NO_PATH)
        {
            tmp = 0;
            if(graph[vertices][end] == INT_MAX_ABSTRACT)
            {
                for (int i = 0; i < vertices; ++i)
                    if(i != end && pathExist(i, end, graph) && sum(graph[vertices][i], graph[i][end]) == INT_MAX_ABSTRACT)
                        tmp++;
                if(tmp >= 2)
                {
                    path[0] = OVERFLOW;
                    path[1] = -1;
                }else
                {
                    for (int pathNode = end; pathNode != begin; pathNode = parent[pathNode])
                        path[indexPath++] = pathNode;
                    path[indexPath++] = begin;

                    path[indexPath] = -1;
                }
            }else if(graph[vertices][end] == INFINITY)
            {
                path[0] = NO_PATH;
                path[1] = -1;
            }else
            {
                for (int pathNode = end; pathNode != begin; pathNode = parent[pathNode])
                    path[indexPath++] = pathNode;
                path[indexPath++] = begin;

                path[indexPath] = -1;
            }

            free(parent);
            free(crossedEnds);

            return path;
        }

        for (int j = 0; j < vertices; ++j)
        {
            tmp = sum(graph[vertices][currentVertex], graph[currentVertex][j]);
            if(j != currentVertex && !isCrossed(j, crossedEnds) && pathExist(currentVertex, j, graph)
               && checkEdgeCapacity(graph[vertices][j], tmp))
            {
                parent[j] = currentVertex;
                graph[vertices][j] = tmp;
            }
        }

        crossedEnds[currentVertex] = CROSSED;
    }
    return NULL;
}

int notCrossedCount(int* crossed, int vertices)
{
    int j = 0;
    for (int i = 0; i < vertices; ++i)
        if(!isCrossed(i, crossed))
            j++;

    return j;
}

void selectNext(int* crossed, const int* graph, int* end, int vertices)
{
    int min = INFINITY, minI = NO_PATH;
    for (int i = 0; i < vertices; ++i)
        if(!isCrossed(i, crossed) && checkGreatest(graph[i], min) == 2)
        {
            min = graph[i];
            minI = i;
        }
    *end = minI;
}

int isCrossed(int index, int* table)
{
    return table[index];
}

int sum(int a, int b)
{
    if(INT_MAX - a < b)
        return INT_MAX_ABSTRACT;
    else if(INT_MAX - b < a)
        return INT_MAX_ABSTRACT;
    else return a + b;
}

int checkGreatest(int a, int b)
{
    if(a == b)
        return 0;
    if(a == INFINITY || b == INFINITY)
    {
        if(a == INFINITY)
            return 1;
        else
            return 2;
    }else if(a == INT_MAX_ABSTRACT || b == INT_MAX_ABSTRACT)
    {
        if(a == INT_MAX_ABSTRACT)
            return 1;
        else
            return 2;
    }else if(a > b)
        return 1;
    else
        return 2;
}

int checkEdgeCapacity(int old, int new)
{
    if(old == new)
    {return NOT_CHANGE;}
    else if(old == INFINITY || old == INT_MAX_ABSTRACT)
    {return MAY_CHANGE;}
    else if(new == INT_MAX_ABSTRACT)
    {return NOT_CHANGE;}
    else if(old > new)
    {return MAY_CHANGE;}
    else
        return NOT_CHANGE;
}

int pathExist(int begin, int end, int** graph)
{
    return graph[begin][end] != 0;
}

int checkRange(int begin, int end, int value)
{
    if(begin <= value && end >= value)
        return 1;
    return 0;
}
