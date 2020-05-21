#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>

typedef struct egde {
    int weight;
    int B;
    int E;
}Edge;

typedef struct tnode {
    int id;
    struct tnode* next;
    struct tnode* last;
}tNode;

int checkRange(int begin, int end, int value);
Edge* kruskal(Edge* graph, int vertices, int edges);
int comparator(const void* a, const void* b);
int err()
{
    printf("bad number of lines");
    return 0;
}

int main()
{
    int vertices = 0;
    int edges = 0;
    if(fscanf(stdin, "%d", &vertices) == EOF)
        return err();
    if(fscanf(stdin, "%d", &edges) == EOF)
        return err();

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
    if(edges == 0 && vertices > 1)
    {
        printf("no spanning tree\n");
        return 0;
    }else if(edges == 0 && vertices == 1)
        return 0;

    Edge* graph;
    graph = calloc((size_t)edges, sizeof(Edge));

    int B1,E1,weight;
    for (int i = 0; i < edges; i++)
    {
        if(fscanf(stdin, "%d %d %d", &B1, &E1, &weight) == EOF)
        {
            free(graph);
            return err();
        }
        if(!checkRange(0, vertices, B1) || !checkRange(0, vertices, E1))
        {
            printf("bad vertex\n");
            free(graph);
            return 0;
        }
        if(!checkRange(0, INT_MAX, weight))
        {
            printf("bad length\n");
            free(graph);
            return 0;
        }

        graph[i].weight = weight;
        graph[i].B = --B1;
        graph[i].E = --E1;
    }

    Edge* ans = kruskal(graph, vertices, edges);
    if(ans == NULL)
    {
        printf("no spanning tree");
        free(graph);
        return 0;
    }else
    {
        int i = 0;
        while(1)
        {
            if(ans[i++].weight != -1)
                printf("%d %d\n", ans[i - 1].B, ans[i - 1].E);
            else
            {
                free(ans);
                free(graph);
                break;
            }
        }
    }
    return 0;
}

Edge* kruskal(Edge* graph, int vertices, int edges)
{
    Edge* result;
    size_t currentSize = 0;
    int freeI = 0;
    result = realloc(NULL, currentSize);
    int* nodes = (int*)calloc((size_t)vertices, sizeof(int));
    tNode** tNodes = (tNode**)malloc((size_t)vertices * sizeof(tNode*));

    for (int k = 0; k < vertices; ++k)
    {
        tNodes[k] = malloc(sizeof(tNode));
        tNodes[k]->id = k;
        tNodes[k]->last = NULL;
        tNodes[k]->next = NULL;
    }

    qsort(graph, (size_t)edges, sizeof(Edge), &comparator);
    tNode* tmp, *tmp1;

    for (int i = 0; i < edges; ++i)
    {
        if(!freeI)
        {
            result = realloc(result, (++currentSize) * sizeof(Edge));
            freeI = 1;
        }

        result[currentSize - 1].B = graph[i].B + 1;
        result[currentSize - 1].E = graph[i].E + 1;

        if(tNodes[graph[i].B]->id == tNodes[graph[i].E]->id)
            continue;
        else
        {
            tmp1 = tmp = tNodes[graph[i].B];
            while(1)
            {
                tmp1->id = tNodes[graph[i].E]->id;
                if(tmp1->last == NULL)
                    break;
                tmp1 = tmp1->last;
            }

            tmp1 = tmp;

            while(1)
            {
                tmp1->id = tNodes[graph[i].E]->id;
                if(tmp1->next == NULL)
                    break;
                tmp1 = tmp1->next;
            }
            tmp1->next = tNodes[graph[i].E];
            tmp = tNodes[graph[i].E];

            while(tmp->last != NULL)
                tmp = tmp->last;
            tmp->last = tmp1;

            freeI = 0;
            nodes[graph[i].B] = 1;
            nodes[graph[i].E] = 1;
        }
    }

    if(!freeI)
        result = realloc(result, (++currentSize) * sizeof(Edge));

    if(currentSize <= 1)
    {
        free(nodes);
        for (int l = 0; l < vertices; ++l)
            free(tNodes[l]);
        free(tNodes);
        free(result);
        return NULL;
    }
    result[currentSize - 1].weight = -1;
    result[currentSize - 1].B = -1;

    freeI = 0;
    for (int j = 0; j < vertices; ++j)
        if(!nodes[j])
        {
            freeI = 1;
            break;
        }

    free(nodes);
    for (int l = 0; l < vertices; ++l)
        free(tNodes[l]);
    free(tNodes);

    if(freeI)
    {
        free(result);
        return NULL;
    }
    return result;
}

int checkRange(int begin, int end, int value)
{
    if(begin <= value && end >= value)
        return 1;
    return 0;
}

int comparator(const void* a, const void* b)
{
    return ((Edge*)a)->weight - ((Edge*)b)->weight;
}