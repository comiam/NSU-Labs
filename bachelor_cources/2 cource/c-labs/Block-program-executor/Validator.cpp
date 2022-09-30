#include "Validator.h"

bool Validator::fileExists(string file)
{
    struct stat buffer{};
    return (stat (file.c_str(), &buffer) == 0);
}
void Validator::badFile(string message)
{
    throw invalid_argument("Bad program file! " + message);
}
