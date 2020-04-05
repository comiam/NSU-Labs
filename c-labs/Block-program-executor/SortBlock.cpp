
#include "SortBlock.h"

bool SortBlock::exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index)
{
    if(!haveInp)
        throw logic_error("null input on sort! Block index: " + to_string(index));
    (*output) = (*input);
    if(input->empty())
        return true;

    sort((*output).begin(), (*output).end(), greater<string>());

    haveOut = true;
    return true;
}