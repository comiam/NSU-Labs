#include <iostream>
#include "ProgramParser.h"

int main(int argc, char* argv[])
{
    if(argc < 2)
        throw std::invalid_argument("Wrong args from prompt!");

    string file = argv[1];
    string input, output;

    if(argc > 2)
    {
        bool wasInput = false;
        bool wasOutput = false;

        for (int i = 2; i < argc; ++i)
        {
            if(((string)"-i") == argv[i] && i != argc && !wasInput)
            {
                wasInput = true;
                input = argv[++i];
            }else if(((string)"-o") == argv[i] && i != argc && !wasOutput)
            {
                wasOutput = true;
                output = argv[++i];
            }else
                throw std::invalid_argument("Wrong args from prompt!");
        }
    }


    ProgramParser::parseProgram("program.txt", input, output).exec();

    return 0;
}