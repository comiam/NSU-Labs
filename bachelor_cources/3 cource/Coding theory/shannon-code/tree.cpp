#include "tree.h"
#include "utils.h"

void clearTree(Node* headTree)
{
    if(headTree == nullptr)
        return;

    clearTree(headTree->right);
    clearTree(headTree->left);
    free(headTree);
}

Node* readTree(std::ifstream &f0, unsigned char* readBytev, int* currentIndex)
{
    if (readBit(f0, readBytev, currentIndex))
    {
        if(checkError())
            return nullptr;

        Node* newNode = new Node;
        newNode->symbol = readByte(f0, readBytev, currentIndex);

        if(checkError())
            return nullptr;

        newNode->right = nullptr;
        newNode->left = nullptr;
        newNode->frequency = 0;

        return newNode;
    }
    else
    {
        if(checkError())
            return nullptr;

        Node* left = readTree(f0, readBytev, currentIndex);
        if(checkError())
            return nullptr;
        Node* right = readTree(f0, readBytev, currentIndex);
        if(checkError())
            return nullptr;

        Node* newNode = new Node;
        newNode->right = right;
        newNode->left = left;
        newNode->frequency = 0;

        return newNode;
    }
}

Node* getTree(Node* symbolData, std::map<char, std::string> *codes)
{
    Node *head  = new Node;
    head->left  = nullptr;
    head->right = nullptr;

    for (const auto& code : *codes)
    {
        Node *tmpHead = head;
        for (char sym : code.second)
        {
            if(sym == '0')
            {
                if(!tmpHead->left)
                {
                    Node *newLeft  = new Node;
                    tmpHead->left  = newLeft;

                    newLeft->left  = nullptr;
                    newLeft->right = nullptr;
                }
                tmpHead = tmpHead->left;
            }else
            {
                if(!tmpHead->right)
                {
                    Node *newRight  = new Node;
                    tmpHead->right  = newRight;

                    newRight->left  = nullptr;
                    newRight->right = nullptr;
                }
                tmpHead = tmpHead->right;
            }
        }
        tmpHead->symbol = code.first;
        //sorry for this...)))) im so lazy to optimize it
        for (int i = 0; i < 256; ++i)
            if(symbolData[i].symbol == code.first)
            {
                tmpHead->frequency = symbolData[i].frequency;
                break;
            }
    }

    return head;
}

void writeEmptyNode(std::ofstream &f0, unsigned char* package, int* index)
{
    writeBit(f0, 1, package, index);
    writeByte(f0, 0, package, index);
}

void writeTree(std::ofstream &f0, Node* headTree, unsigned char* package, int* index)
{
    if(!(headTree->right) && !(headTree->left))
    {
        writeBit(f0, 1, package, index);
        writeByte(f0, headTree->symbol, package, index);
    }else
    {
        writeBit(f0, 0, package, index);
        if(headTree->left)
            writeTree(f0, headTree->left, package, index);
        else
            /*its stub, this algorithm of writing tree created for huffman trees, not for shannon
              huffman tree always contains left and right leaves in comparison with shannon code tree
             */
            writeEmptyNode(f0, package, index);

        if(headTree->right)
            writeTree(f0, headTree->right, package, index);
        else
            writeEmptyNode(f0, package, index);
    }
}