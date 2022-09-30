#ifndef INC_0B_CSVPARSER_H
#define INC_0B_CSVPARSER_H

#include <fstream>
#include <typeinfo>
#include <regex>
#include "TupleUtils.h"

using namespace std;

template<class ... Args>
class CSVParser
{
private:
    ifstream &input;
    size_t offset;
    int fileLength = -1;
    char fieldDelimiter = '\"';
    char columnDelimiter = ',';
    char lineDelimiter = '\n';

    enum class ParsingState
    {
        simpleRead,
        delimiterReading
    };

    template<typename _CharT, typename _Traits, typename _Alloc>
    void getLine(basic_istream<_CharT, _Traits> &is, basic_string<_CharT, _Traits, _Alloc> &str)
    {
        str.clear();

        char c;
        while(is.get(c))
        {
            if(c == lineDelimiter)
                break;
            str.push_back(c);
        }
    }

    bool emptyTailToDelimiter(string a, int pos)
    {
        for(int i = pos; i < a.size(); ++i)
            if(a[i] == columnDelimiter)
                break;
            else
                if(a[i] != ' ')
                    return false;

        return true;
    }

    string ltrim(const string& str)
    {
        return regex_replace(str, regex("^\\s+"), string(""));
    }

    string rtrim(const string& str)
    {
        return regex_replace(str, regex("\\s+$"), string(""));
    }

    string trim(const string& str)
    {
        return ltrim(rtrim(str));
    }


    int getLength()
    {
        if(fileLength == -1)
        {
            input.clear();
            input.seekg(0, ios::beg);

            string line;
            for(fileLength = 0; getline(input, line); fileLength++);

            input.clear();
            input.seekg(0, ios::beg);
        }
        return fileLength;
    }

    class CSVIterator
    {
    private:
        string strBuffer;
        ifstream &input;
        size_t index;
        CSVParser<Args...> &parent;
        bool last = false;

        friend class CSVParser;

    public:
        CSVIterator(ifstream &ifs, size_t index, CSVParser<Args...> &parent) : index(index), parent(parent), input(ifs)
        {
            for(int i = 0; i < index - 1; i++, parent.getLine(input, strBuffer));

            parent.getLine(input, strBuffer);
            if(!input)
                throw logic_error("Bad file!");
        }

        CSVIterator operator++()
        {
            if(index < parent.fileLength)
            {
                index++;
                input.clear();
                input.seekg(0, ios::beg);
                for(int i = 0; i < index - 1; ++i, parent.getLine(input, strBuffer));

                parent.getLine(input, strBuffer);
            } else
            {
                strBuffer = "";
                last = true;
            }

            return *this;
        }

        bool operator==(const CSVIterator &other) const
        {
            return this->last == other.last && this->index == other.index && this->strBuffer == other.strBuffer;
        }

        bool operator!=(const CSVIterator &other)
        {
            return !(*this == other);
        }

        tuple<Args...> operator*()
        {
            return parent.parseLine(strBuffer, index);
        }
    };

public:
    explicit CSVParser(ifstream &ifs, size_t offset) : input(ifs), offset(offset)
    {
        if(!ifs.is_open())
            throw std::invalid_argument("Can't open file");
        if(offset >= getLength())
            throw logic_error("Bad file offset! offset >= file");
        if(offset < 0)
            throw logic_error("Bad file offset! offset < 0");
    }

    void setDelimiters(char newFieldDelimiter, char newColumnDelimiter, char newLineDelimiter)
    {
        lineDelimiter = newLineDelimiter;
        fieldDelimiter = newFieldDelimiter;
        columnDelimiter = newColumnDelimiter;
    }

    void reset()
    {
        input.clear();
        input.seekg(0, ios::beg);
    }

    CSVIterator begin()
    {
        CSVIterator a(input, offset + 1, *this);
        return a;
    }

    CSVIterator end()
    {
        int t = getLength();

        CSVIterator a(input, 1, *this);
        a.last = true;
        a.strBuffer = "";
        a.index = getLength();
        return a;
    }

    vector<string> read_string(string &line, int lineNum)
    {
        vector<string> fields {""};
        ParsingState state = ParsingState::simpleRead;
        size_t fcounter = 0;
        size_t linePos = 0;
        bool filled = false;
        bool accessWriteDelim = false;
        line = trim(line);
        for(char c:line)
        {
            if(state == ParsingState::simpleRead)
            {
                if(c == columnDelimiter)
                {
                    fields[fcounter] = trim(fields[fcounter]);
                    fields.emplace_back("");
                    fcounter++;
                    filled = false;
                } else if(c == fieldDelimiter)
                {
                    if(linePos > 0 && filled)
                        throw invalid_argument("Bad " + to_string(fcounter) + "th field at line " + to_string(lineNum) +
                                               ": field delimiter not first!");

                    fields[fcounter] = trim(fields[fcounter]);
                    state = ParsingState::delimiterReading;
                    accessWriteDelim = false;
                } else
                {
                    if(c != ' ')
                        filled = true;

                    fields[fcounter].push_back(c);
                }
            } else //TODO DELIMITER READING
            {
                if(c == fieldDelimiter)
                {
                    if(!accessWriteDelim && line.size() > linePos + 1 && line[linePos + 1] != fieldDelimiter)
                    {
                        if(!emptyTailToDelimiter(line, linePos + 1))
                            throw invalid_argument(
                                    "Bad " + to_string(fcounter) + "th field at line " + to_string(lineNum) +
                                    ": symbols after delimiter in field!");
                        state = ParsingState::simpleRead;
                    } else if(!accessWriteDelim && linePos == line.size() - 1)
                    {
                        state = ParsingState::simpleRead;
                    }else if(!accessWriteDelim)
                        accessWriteDelim = true;
                    else
                    {
                        fields[fcounter].push_back(c);
                        accessWriteDelim = false;
                    }
                } else
                    fields[fcounter].push_back(c);
            }
            linePos++;
        }
        if(state != ParsingState::simpleRead)
            throw invalid_argument(
                    "Bad " + to_string(fcounter) + "th field at line " + to_string(lineNum) + ": not closed field!");
        return fields;
    }

    tuple<Args...> parseLine(string &line, int number)
    {
        size_t size = sizeof...(Args);

        if(line.empty())
            throw invalid_argument("Line " + to_string(number) + " is empty!");

        tuple<Args...> table_str;
        vector<string> fields = read_string(line, number);

        if(fields.size() != size)
            throw invalid_argument("Wrong number of fields in line " + to_string(number) + "!");

        auto a = fields.begin();
        try
        {
            parser_utils::parse(table_str, a);
        }catch(exception&e)
        {
            throw invalid_argument("Line " + to_string(number) + " contains bad types!");
        }

        return table_str;
    }
};

#endif