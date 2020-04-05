#ifndef BLOCKEXEC_FILEREADERBLOCK_H
#define BLOCKEXEC_FILEREADERBLOCK_H

#include "Worker.h"
#include "Validator.h"
#include <fstream>

class FileReaderBlock : public Worker
{
public:
    FileReaderBlock(string);
    FileReaderBlock(string &&);
    static void readFile(string, vector<string>&);

    bool exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index) override;
};


#endif
