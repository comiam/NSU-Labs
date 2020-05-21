#include "FileWriterBlock.h"
#include "Validator.h"

FileWriterBlock::FileWriterBlock(string &&inputFile)
{
    args.push_back(inputFile);
    inputFile = nullptr;
}

FileWriterBlock::FileWriterBlock(string inputFile)
{
    args.push_back(inputFile);
}

void FileWriterBlock::writeFile(string output, vector<string> &text)
{
    ofstream fos(output);

    if(!fos)
        Validator::badFile("File " + output + " cannot opened!");

    for(const auto &i : text)
        fos << i << endl;

    fos.close();
}

bool FileWriterBlock::exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index)
{
    if(!haveInp)
        throw logic_error("null input on write file: " + args[0] + ". Block index: " + to_string(index));

    ofstream fos(args[0]);

    if(!fos)
    {
        Validator::badFile("File " + args[0] + " cannot opened!");
        return false;
    }

    for(const auto &i : *input)
        fos << i << endl;

    fos.close();

    haveOut = false;
    return true;
}
