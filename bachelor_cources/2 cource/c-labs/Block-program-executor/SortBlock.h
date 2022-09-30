#ifndef BLOCKEXEC_SORTBLOCK_H
#define BLOCKEXEC_SORTBLOCK_H

#include "Worker.h"
#include <algorithm>

class SortBlock: public Worker
{
public:
    bool exec(vector<string> *input, vector<string>* output, bool &haveInp, bool &haveOut, unsigned int index) override;
};


#endif