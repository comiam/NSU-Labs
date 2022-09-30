#include "FileReaderBlock.h"

FileReaderBlock::FileReaderBlock(string && inputFile)
{
    args.push_back(inputFile);
    inputFile = nullptr;
}

FileReaderBlock::FileReaderBlock(string inputFile)
{
    args.push_back(inputFile);
}

void FileReaderBlock::readFile(string input, vector<string>& output)
{
    if(!Validator::fileExists(input))
        Validator::badFile("File " + input + " cannot exists!");

    string line;
    ifstream fis(input);

    if(!fis)
        Validator::badFile("File " + input + " cannot opened!");

    while (getline(fis, line))
        output.push_back(line);

    fis.close();
}

bool FileReaderBlock::exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index)
{
    if(haveInp)
        throw logic_error("non null input on read file: " + args[0] + ". Block index: " + to_string(index));
    if(!Validator::fileExists(args[0]))
    {
        Validator::badFile("File " + args[0] + " cannot exists!");
        return false;
    }

    string line;
    ifstream fis(args[0]);

    if(!fis)
    {
        Validator::badFile("File " + args[0] + " cannot opened!");
        return false;
    }

    while (getline(fis, line))
        output->push_back(line);

    fis.close();

    haveOut = true;

    return true;
}
