
#include "ReplaceBlock.h"

ReplaceBlock::ReplaceBlock(string &&from, string &&to)
{
    args.push_back(from);
    args.push_back(to);
    from = nullptr;
    to = nullptr;
}

ReplaceBlock::ReplaceBlock(string from, string to)
{
    args.push_back(from);
    args.push_back(to);
}

bool ReplaceBlock::exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index1)
{
    if(!haveInp)
        throw logic_error("null input on text replace! Block index: " + to_string(index1));

    (*output) = (*input);
    if(input->empty())
        return true;

    size_t index;
    for(string &i : *output)
        while ((index = i.find(args[0])) != string::npos)
            i.replace(index, args[0].size(), args[1]);

    haveOut = true;

    return true;
}
