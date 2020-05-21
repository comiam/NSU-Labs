#include <stdio.h>
#include <malloc.h>

typedef struct node
{
    int value;
	unsigned char height;
    struct node* left;
    struct node* right;
} AVLNode;

AVLNode* create(int value);
unsigned char getHeight(AVLNode* n);
int getBF(AVLNode* p);
void fixHeight(AVLNode* p);
AVLNode* rotateLeft(AVLNode* n);
AVLNode* rotateRight(AVLNode* n);
AVLNode* balance(AVLNode* n);
AVLNode* insertNode(AVLNode* n, int v);
void clear(AVLNode* head);

int main()
{
    int n;
    scanf("%d", &n);

    int tmp = 0;
    AVLNode* head = NULL;

    for (int i = 0; i < n; ++i)
    {
        scanf("%d", &tmp);
        head = insertNode(head, tmp);
    }
    printf("%d", getHeight(head));
    clear(head);

    return 0;
}

void clear(AVLNode* head)
{
    if(head == NULL)
        return;

    clear(head->right);
    clear(head->left);
    free(head);
}

AVLNode* insertNode(AVLNode* n, int v) // вставка ключа k в дерево с корнем p
{
    if(!n)
        return create(v);
    if(v < n->value)
        n->left = insertNode(n->left, v);
    else
        n->right = insertNode(n->right, v);
    return balance(n);
}

AVLNode* balance(AVLNode* n) // балансировка узла p
{
    fixHeight(n);
    if(getBF(n) == 2)
    {
        if(getBF(n->right) < 0)
            n->right = rotateRight(n->right);
        return rotateLeft(n);
    }
    if(getBF(n) == -2)
    {
        if(getBF(n->left) > 0)
            n->left = rotateLeft(n->left);
        return rotateRight(n);
    }
    return n; // в противном случае балансировка не нужна
}

AVLNode* rotateLeft(AVLNode* n) // левый поворот вокруг q
{
    AVLNode* p = n->right;
    n->right = p->left;
    p->left = n;
    fixHeight(n);
    fixHeight(p);
    return p;
}

AVLNode* rotateRight(AVLNode* n) // правый поворот вокруг p
{
    AVLNode* q = n->left;
    n->left = q->right;
    q->right = n;
    fixHeight(n);
    fixHeight(q);
    return q;
}

void fixHeight(AVLNode* p)
{
    unsigned char nl = getHeight(p->left);
    unsigned char nr = getHeight(p->right);
    p->height = (nl > nr ? nl : nr) + (unsigned char)1;
}

int getBF(AVLNode* p)
{
    return getHeight(p->right) - getHeight(p->left);
}

unsigned char getHeight(AVLNode* n)
{
    return n ? n->height : (unsigned char)0;
}

AVLNode* create(int value)
{
    AVLNode* node = malloc(sizeof(AVLNode));
    node->value = value;
    node->left = node->right = 0;
    node->height = 1;
    return node;
}