#include <fcntl.h>
#include "encoder.h"
#include "decoder.h"

int shannon(bool compress, std::ifstream& f0, std::ofstream& f1);

int main(int argc, char* argv[])
{
    std::ifstream f0;
    std::ofstream f1;
    char* code;
    bool compress;

    if(argc == 4)
    {
        code = argv[1];

        if(strcmp("-c", code) && strcmp("-d", code) && strcmp("c", code) && strcmp("d", code))
            argError

        compress = !strcmp("-c", code) || !strcmp("c", code);

        f0.open(argv[2], std::ios::in | std::ios::binary);
        f1.open(argv[3], std::ios::out | std::ios::binary);
    }
    else
        argError

    return shannon(compress, f0, f1);
}

int shannon(bool compress, std::ifstream& f0, std::ofstream& f1)
{
    if(!f0 || !f1)
    {
        fileError
        if(f0)
            f0.close();
        if(f1)
            f1.close();

        return 0;
    }

    if(compress)
    {
        auto* probTable = getProbTable(f0);
        auto* codeTable = getCodeTable(probTable);
        auto* headTree  = getTree(probTable, codeTable);

        bitEncode(f0, f1, headTree, codeTable);
        clearTree(headTree);

        delete [] probTable;
        delete codeTable;
    }else
    {
        unsigned char packageByte = 0;
        int packageIndex = 0;
        f0.read(reinterpret_cast<char *>(&packageByte), 1);

        auto tail = getTail(f0, &packageByte, &packageIndex);
        checkErrorReading(f0.close(), f1.close(), ;)

        Node* headTree = readTree(f0, &packageByte, &packageIndex);
        checkErrorReading(f0.close(), f1.close(), clearTree(headTree))

        gotoNextByte(f0, &packageByte, &packageIndex);
        checkErrorReading(f0.close(), f1.close(), clearTree(headTree))

        bitDecode(f0, f1, &packageByte, &packageIndex, headTree, tail);
        checkErrorReading(f0.close(), f1.close(), clearTree(headTree))

        clearTree(headTree);
    }

    f0.close();
    f1.close();

    return 0;
}
