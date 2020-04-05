#include "GrepBlock.h"

GrepBlock::GrepBlock(string && find)
{
    args.push_back(find);
    find = nullptr;
}

GrepBlock::GrepBlock(string find)
{
    args.push_back(find);
}

bool GrepBlock::exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index)
{
    if(!haveInp)
        throw logic_error("null input on grep! Block index: " + to_string(index));
    if(input->empty())
    {
        (*output) = (*input);
        return true;
    }

    (*output).clear();

    for(const auto &i : *input)
        if(i.find(args[0]) != string::npos)
            output->push_back(i);

    haveOut = true;

    return true;
}