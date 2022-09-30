#ifndef PERCEPTRON_IO_H
#define PERCEPTRON_IO_H

#include "util.h"

void saveError(const NdArrayF &data, const std::string &fileName)
{
    auto outf = std::ofstream("../pyWrapper/" + fileName);

    if (!outf)
    {
        std::cerr << "Uh oh, .txt could not be opened for writing!" << std::endl;
        exit(1);
    }
    for (const auto &elem: data)
    {
        outf << elem << std::endl;
    }
}


void saveTestErrors(const std::pair<std::vector<NdArrayF>, std::vector<float>> &data)
{
    auto sinOut = std::ofstream("../pyWrapper/G_dev.txt");
    auto CosOut = std::ofstream("../pyWrapper/kgf.txt");
    auto testMse = std::ofstream("../pyWrapper/testMse.txt");

    auto inf = std::ofstream("in.txt");

    if (!sinOut)
    {
        std::cerr << "Uh oh, .txt could not be opened for writing!" << std::endl;
        exit(1);
    }
    for (const auto &elem: data.first)
    {
        sinOut << elem[0] << std::endl;
        CosOut << elem[1] << std::endl;

    }
    for (const auto &elem:data.second)
    {
        testMse << elem << std::endl;
    }

}

auto parse_csv(std::istream &file, char delimiter, unsigned target_columns_count) -> DataSet
{
    auto line = std::string();
    auto result = DataSet();

    while (std::getline(file, line))
    {
        auto line_buffer = std::istringstream(line);
        auto token = std::string();
        auto row = std::vector<float>();

        while (std::getline(line_buffer, token, delimiter))
            row.push_back(std::stod(token));

        row.erase(row.begin());

        auto array_row = nc::NdArray(row);
        result.emplace_back(
                array_row[nc::Slice(-target_columns_count)],
                array_row[nc::Slice(-target_columns_count, array_row.size())]);
    }

    return result;
}

#endif
