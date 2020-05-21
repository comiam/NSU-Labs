#ifndef BLOCKEXEC_VALIDATOR_H
#define BLOCKEXEC_VALIDATOR_H

#include <string>
#include <sys/stat.h>
#include <stdexcept>


using namespace std;

class Validator
{
public:
    static bool fileExists(string file);
    static void badFile(string message);
};


#endif