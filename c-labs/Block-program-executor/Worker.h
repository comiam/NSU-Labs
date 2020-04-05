#ifndef BLOCKEXEC_WORKER_H
#define BLOCKEXEC_WORKER_H

#include <string>
#include <vector>
#include <stdexcept>

using namespace std;

class Worker
{
protected:
    vector<string> args;
public:
    virtual ~Worker() = default;
    virtual bool exec(vector<string> *input, vector<string> *output, bool &haveInp, bool &haveOut, unsigned int index) = 0;
};

#endif