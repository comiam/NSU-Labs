#include "BlockProgram.h"

void BlockProgram::exec()
{
    if(queue.empty() || blocks.empty())
        return;

    bool haveInp = false, haveOut = false;
    vector<string> textI;
    vector<string> textO;
    if(!dynamic_cast<FileReaderBlock*>(blocks[queue[0]].get()) && !input.empty())
        FileReaderBlock::readFile(input, textI);

    for(int i : queue)
    {
        blocks[i]->exec(&textI, &textO, haveInp, haveOut, i);
        textI = textO;
        haveInp = haveOut;
    }

    if(!dynamic_cast<FileWriterBlock*>(blocks[queue[0]].get()) && !output.empty())
        FileWriterBlock::writeFile(output, textO);
}

BlockProgram::~BlockProgram()
{
    blocks.clear();
    queue.clear();
}