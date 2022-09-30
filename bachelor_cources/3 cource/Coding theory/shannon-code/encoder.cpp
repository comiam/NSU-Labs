#include "encoder.h"

Node* getProbTable(std::ifstream &file)
{
    unsigned char buf[DEFAULT_BLOCK_SIZE];

    auto* table = new Node[256];
    for (int i = 0; i < 256; ++i)
    {
        table[i].symbol = i;
        table[i].probability = 0;
        table[i].frequency = 0;
    }

    size_t i, final_size = 0;

    while(true)
    {
        file.read(reinterpret_cast<char *>(&buf[0]), DEFAULT_BLOCK_SIZE);
        if(!(i = file.gcount()))
            break;

        final_size += i;
        for (int j = 0; j < i; ++j)
            table[buf[j]].frequency++;
    }

    for (int j = 0; j < 256; ++j)
        table[j].probability = table[j].frequency / (double)final_size;

    return table;
}

std::map<char, std::string>* getCodeTable(Node *symbolData)
{
    qsort(symbolData, 256, sizeof(Node), &comparator);

    int totalCodeCount = 0;
    for (int i = 0; i < 256; ++i)
        if(symbolData[i].probability != 0)
            totalCodeCount++;

    auto *cumulative = new double[totalCodeCount];
    for (int i = 0; i < totalCodeCount; ++i)
        cumulative[i] = 0;

    for (int i = 0; i < totalCodeCount - 1; ++i)
        cumulative[i + 1] = cumulative[i] + symbolData[i].probability;

    auto* codes = new std::map<char, std::string>();

    for (int i = 0; i < totalCodeCount; ++i)
    {
        int length = (int)std::ceil(-std::log2(symbolData[i].probability));

        double savedCumulative = cumulative[i];
        (*codes)[symbolData[i].symbol] = "";

        for (int j = 0; j < length; ++j)
        {
            savedCumulative *= 2;
            auto sym = savedCumulative >= 1.0 ? "1" : "0";
            savedCumulative = fmod(savedCumulative, 1.0);
            (*codes)[symbolData[i].symbol] += sym;
        }
        //std::cout << symbolData[i].symbol << " " << (*codes)[symbolData[i].symbol] << std::endl;
    }

    delete[] cumulative;

    return codes;
}

void bitEncode(std::ifstream &f0, std::ofstream &f1, Node *headTree, std::map<char, std::string> *codeTable)
{
    f0.clear();
    f0.seekg (0, std::ios::beg);
    unsigned char packageByte = 0;
    int packageIndex = 0;

    //printf("%lu\n", getCompressedDataSize(headTree, codeTable) % 8);

    switch(getCompressedDataSize(headTree, codeTable) % 8)
    {
        case 0:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 1:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        case 2:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 3:
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        case 4:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 5:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        case 6:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 0, &packageByte, &packageIndex);
            break;
        case 7:
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            writeBit(f1, 1, &packageByte, &packageIndex);
            break;
        default:
            printf("Error during writing!!!\n");
            return;
    }

    writeTree(f1, headTree, &packageByte, &packageIndex);
    writeLastByte(f1, &packageByte, &packageIndex);

    unsigned char buf[DEFAULT_BLOCK_SIZE];
    size_t i;

    while(true)
    {
        f0.read(reinterpret_cast<char*>(buf), DEFAULT_BLOCK_SIZE);
        if(!(i = f0.gcount()))
            break;

        for (int j = 0; j < i; ++j)
        {
            for (auto &sym : (*codeTable)[buf[j]])
                if (sym == '0')
                    writeBit(f1, 0, &packageByte, &packageIndex);
                else if (sym == '1')
                    writeBit(f1, 1, &packageByte, &packageIndex);
        }
    }
    writeLastByte(f1, &packageByte, &packageIndex);
}

unsigned long getCompressedDataSize(Node* headTree, std::map<char, std::string>* codeTable)
{
    if(!headTree)
        return 0;
    if(!(headTree->right) && !(headTree->left))
        return (*codeTable)[headTree->symbol].length() * headTree->frequency;
    else
        return getCompressedDataSize(headTree->left, codeTable) + getCompressedDataSize(headTree->right, codeTable);
}