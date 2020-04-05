#include "DumpBlock.h"

DumpBlock::DumpBlock(string && output)
{
    args.push_back(output);
    output = nullptr;
}

DumpBlock::DumpBlock(string output)
{
    args.push_back(output);
}

bool DumpBlock::exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index)
{
    if(!haveInp)
        throw logic_error("null input on dump! Block index: " + to_string(index));

    (*output) = (*input);
    ofstream fos(args[0]);

    if(!fos)
    {
        Validator::badFile("File " + args[0] + " cannot opened!");
        return false;
    }

    for(const auto &i : *input)
        fos << i << endl;

    fos.close();

    haveOut = true;
    return true;
}

