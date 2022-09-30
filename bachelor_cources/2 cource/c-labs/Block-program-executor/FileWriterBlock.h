#ifndef BLOCKEXEC_FILEWRITERBLOCK_H
#define BLOCKEXEC_FILEWRITERBLOCK_H

#include "Worker.h"
#include <fstream>

class FileWriterBlock: public Worker
{
public:
    FileWriterBlock(string inputFile);
    FileWriterBlock(string &&);
    static void writeFile(string, vector<string>&);
    bool exec(vector<string> *input, vector<string>* output, bool &haveInp, bool &haveOut, unsigned int index) override;
};

#endif