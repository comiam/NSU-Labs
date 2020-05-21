#include "CSVParser.h"

using namespace std;

int main(int argc, char *argv[])
{
    ifstream a("input.csv");

    CSVParser<string, double> parser(a,0);
    parser.setDelimiters('\"',';','/');

    for(tuple<string, double> el : parser)
        cout << el << endl;

    return 0;
}