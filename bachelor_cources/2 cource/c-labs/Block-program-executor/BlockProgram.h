#ifndef BLOCKEXEC_BLOCKPROGRAM_H
#define BLOCKEXEC_BLOCKPROGRAM_H

#include "Worker.h"
#include "FileReaderBlock.h"
#include "FileWriterBlock.h"
#include <map>
#include <utility>
#include <memory>

class BlockProgram
{
private:
    map<unsigned int, std::shared_ptr<Worker>> blocks;
    vector<int> queue;
    string input;
    string output;
public:
    BlockProgram(map<unsigned int, std::shared_ptr<Worker>> blocks_, vector<int> queue_, string input_, string output_) : blocks(
            move(blocks_)), queue(move(queue_)), input(move(input_)), output(move(output_)) {}
    ~BlockProgram();
    void exec();
};

#endif
