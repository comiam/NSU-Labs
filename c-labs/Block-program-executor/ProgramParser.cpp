#include "ProgramParser.h"

shared_ptr<Worker> parseBlock(ifstream& fis)//RVO
{
    string tmp0, tmp1;
    fis >> tmp0;

    if(tmp0 != "=")
        Validator::badFile("Haven't '=' at block part!");

    fis >> tmp0;
    shared_ptr<Worker> block;
    if(tmp0 == "readfile")
    {
        fis >> tmp1;
        block = make_shared<FileReaderBlock>(tmp1);
        return block;
    }else if(tmp0 == "writefile")
    {
        fis >> tmp1;
        block = make_shared<FileWriterBlock>(tmp1);
        return block;
    }else if(tmp0 == "grep")
    {
        fis >> tmp1;
        block = make_shared<GrepBlock>(tmp1);
        return block;
    }else if(tmp0 == "sort")
    {
        block = make_shared<SortBlock>();
        return block;
    }else if(tmp0 == "replace")
    {
        fis >> tmp0 >> tmp1;
        block = make_shared<ReplaceBlock>(tmp0, tmp1);
        return block;
    }else if(tmp0 == "dump")
    {
        fis >> tmp1;
        block = make_shared<DumpBlock>(tmp1);
        return block;
    }else
    {
        Validator::badFile("Bad block name: " + tmp0);
        return nullptr;
    }
}

bool parseBlockPart(map<unsigned int, shared_ptr<Worker>>& map, ifstream& fis)
{
    if(fis.eof())
        Validator::badFile("EOF file!");
    string tmp0;
    fis >> tmp0;
    unsigned int index;
    if(tmp0 != "desc")
        Validator::badFile("Haven't 'desc' at block part!");

    while(true)
    {
        try
        {
            if(fis.eof())
                Validator::badFile("Bad end of block part!");
            fis >> tmp0;
            if(tmp0 == "csed")
                break;

            index = stoi(tmp0);
            if(map.count(index))
                Validator::badFile("Index " + to_string(index) + " already exists!");
            else
                map[index] = parseBlock(fis);

        } catch(exception& e) {
            throw invalid_argument(e.what());
        }
    }

    return true;
}

bool parseQueue(vector<int>& queue, ifstream& fis)
{
    if(fis.eof())
        Validator::badFile("EOF file!");
    string tmp0;
    unsigned int index;
    while(true)
    {
        try
        {
            if(fis.eof())
                break;

            fis >> tmp0;
            index = stoi(tmp0);
            queue.push_back(index);

            if(fis.eof())
                break;

            fis >> tmp0;
            if(tmp0 != "->")
                Validator::badFile("Unknown symbol at queue part: " + tmp0);
        } catch(exception& e) {
            throw invalid_argument(e.what());
        }
    }
    return true;
}

BlockProgram ProgramParser::parseProgram(string file, string input, string output)
{
    if(!Validator::fileExists(file) || (!input.empty() && !Validator::fileExists(input)))
        throw invalid_argument("Wrong args from prompt!");

    unsigned int codeError = 0;

    ifstream fis(file);

    map<unsigned int, shared_ptr<Worker>> blocks;
    if(!parseBlockPart(blocks, fis))
        codeError = 1;

    vector<int> queue;
    if(!codeError && !parseQueue(queue, fis))
        codeError = 2;

    if(!codeError && !dynamic_cast<FileReaderBlock*>(blocks[queue[0]].get()) && input.empty())
        codeError = 3;

    if(!codeError && !dynamic_cast<FileWriterBlock*>(blocks[queue[queue.size() - 1]].get()) && output.empty())
        codeError = 4;

    fis.close();

    if(codeError)
    {
        queue.clear();
        blocks.clear();

        switch(codeError)
        {
            case 1:
                Validator::badFile("Scanning block part failed!");
            case 2:
                Validator::badFile("Scanning queue part failed!");
            case 3:
                throw invalid_argument("Haven't input!");
            case 4:
                throw invalid_argument("Haven't output!");
        }
    }

    return BlockProgram(blocks, queue, move(input), move(output));
}