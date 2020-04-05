#ifndef BLOCKEXEC_REPLACEBLOCK_H
#define BLOCKEXEC_REPLACEBLOCK_H

#include "Worker.h"

class ReplaceBlock: public Worker
{
public:
    ReplaceBlock(string, string);
    ReplaceBlock(string &&, string &&);
    bool exec(vector<string> *input, vector<string>* output, bool &haveInp, bool &haveOut, unsigned int index) override;
};


#endif