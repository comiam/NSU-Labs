#include <cstdio>
#include <cstdlib>
#include <iostream>
#include "optionparser.h"

#include "GameCore.h"

using namespace std;

struct Arg : public option::Arg
{
    static void printError(const char *msg1, const option::Option &opt, const char *msg2)
    {
        fprintf(stderr, "%s", msg1);
        fwrite(opt.name, opt.namelen, 1, stderr);
        fprintf(stderr, "%s", msg2);
    }

    static option::ArgStatus Unknown(const option::Option &option, bool msg)
    {
        if(msg) printError("Unknown option '", option, "'\n");
        return option::ARG_ILLEGAL;
    }

    static option::ArgStatus Numeric(const option::Option &option, bool msg)
    {
        char *endptr = 0;
        if(option.arg != 0 && strtol(option.arg, &endptr, 10))
        {};
        if(endptr != option.arg && *endptr == 0)
            return option::ARG_OK;

        if(msg) printError("Option '", option, "' requires a numeric argument\n");
        return option::ARG_ILLEGAL;
    }
};

enum optionIndex
{
    UNKNOWN, HELP, FIRST, SECOND, COUNT
};
const option::Descriptor usage[] = {
        {UNKNOWN, 0, "",  "",       Arg::Unknown,  "USAGE: example_arg [options]\n\n"
                                                   "Options:"},
        {HELP,    0, "",  "help",   Arg::None,     "  \t--help  \tYou can choose the type of two players and the number of rounds in the series. Total player types 3:\n\t\t\tRandom - random player. Absolutely random AI;\n\t\t\tOptional - AI with if else c: ;\n\t\t\tInteractive - it's are you;) .\t"},
        {FIRST,   0, "f", "first",  Arg::Optional, "  -f[<arg>], \t--first[=<arg>]"
                                                   "  \tType of first player. Default is Random."},
        {SECOND,  0, "s", "second", Arg::Optional, "  -s[<arg>], \t--second[=<arg>]"
                                                   "  \tType of second player. Default is Random."},
        {COUNT,   0, "c", "count",  Arg::Numeric,  "  -c <num>, \t--count=<num>  \tNumber of rounds in a series. Default is 1."},
        {0,       0, 0,   0,        0,             0}};

int main(int argc, char *argv[])
{
    argc -= (argc > 0);
    argv += (argc > 0); // skip program name argv[0] if present
    option::Stats stats(usage, argc, argv);

#ifdef __GNUC__
    option::Option options[stats.options_max], buffer[stats.buffer_max];
#else
    option::Option* options = (option::Option*)calloc(stats.options_max, sizeof(option::Option));
    option::Option* buffer  = (option::Option*)calloc(stats.buffer_max,  sizeof(option::Option));
#endif

    option::Parser parse(usage, argc, argv, options, buffer);
    if(parse.error())
        return 1;

    if(options[HELP] || argc == 0)
    {
        int columns = getenv("COLUMNS") ? atoi(getenv("COLUMNS")) : 80;
        option::printUsage(fwrite, stdout, usage, columns);
        return 0;
    }

    if(options[UNKNOWN])
        return 0;

    if(parse.nonOptionsCount() > 0)
    {
        for(int i = 0; i < parse.nonOptionsCount(); ++i)
            fprintf(stdout, "Unknown argument #%d is %s\n", i, parse.nonOption(i));
        return 0;
    }

    int count = 1;
    PlayerType f = PlayerType ::Random, s = PlayerType::Random;

    if(options[COUNT])
    {
        count = strtol(options[COUNT].arg, nullptr, 10);
        if(count < 0)
        {
            fprintf(stdout, "Number of rounds must be positive!\n");
            return 0;
        }else if(count == 0)
            return 0;
    }

    if(options[FIRST])
    {
        if(((string)"Random") == options[FIRST].arg)
            f = PlayerType::Random;
        else if(((string)"Optional") == options[FIRST].arg)
            f = PlayerType::Optional;
        else if(((string)"Interactive") == options[FIRST].arg)
            f = PlayerType::Interactive;
        else
        {
            fprintf(stdout, "Unknown player type: %s!", options[FIRST].arg);
            return 0;
        }
    }

    if(options[SECOND])
    {
        if(((string)"Random") == options[SECOND].arg)
            s = PlayerType::Random;
        else if(((string)"Optional") == options[SECOND].arg)
            s = PlayerType::Optional;
        else if(((string)"Interactive") == options[SECOND].arg)
            s = PlayerType::Interactive;
        else
        {
            fprintf(stdout, "Unknown player type: %s!", options[SECOND].arg);
            return 0;
        }
    }

    run(count, f, s);
}
