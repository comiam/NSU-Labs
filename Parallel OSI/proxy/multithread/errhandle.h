#ifndef LAB10_ERRHANDLE_H
#define LAB10_ERRHANDLE_H

#include <cstdio>

//#define DEBUG_ENABLED

#define errorf(Format, ...)         fprintf(stderr, "[ERROR] " Format, ##__VA_ARGS__)
#define errorfln(Format, ...)       errorf(Format "\n", ##__VA_ARGS__)

#define NO_ERROR                    0

#endif
