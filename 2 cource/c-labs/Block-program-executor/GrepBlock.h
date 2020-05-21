#ifndef BLOCKEXEC_GREPBLOCK_H
#define BLOCKEXEC_GREPBLOCK_H

#include "Worker.h"

class GrepBlock: public Worker
{
public:
    GrepBlock(string);
    GrepBlock(string &&);
    bool exec(vector<string> *input, vector<string>* output, bool &haveInp, bool &haveOut, unsigned int index) override;
};


#endif