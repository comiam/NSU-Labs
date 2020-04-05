#ifndef BLOCKEXEC_DUMPBLOCK_H
#define BLOCKEXEC_DUMPBLOCK_H

#include "Worker.h"
#include "Validator.h"
#include <fstream>

class DumpBlock: public Worker
{
public:
    DumpBlock(string);
    DumpBlock(string &&);
    bool exec(vector<string> *input, vector<string>* output, bool &haveInp, bool &haveOut, unsigned int index) override;
};


#endif
