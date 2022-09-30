#ifndef BLOCKEXEC_PROGRAMPARSER_H
#define BLOCKEXEC_PROGRAMPARSER_H

#include "BlockProgram.h"
#include "Validator.h"
#include "SortBlock.h"
#include "FileReaderBlock.h"
#include "FileWriterBlock.h"
#include "GrepBlock.h"
#include "ReplaceBlock.h"
#include "DumpBlock.h"
#include <utility>
#include <fstream>

class ProgramParser
{
public:
    static BlockProgram parseProgram(string file, string input = "", string output = "");
};


#endif