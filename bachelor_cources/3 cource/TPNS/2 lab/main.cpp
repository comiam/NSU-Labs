#include <iostream>
#include "NumCpp.hpp"
#include "Perceptron.h"
#include "io.h"

constexpr auto input_size = 15;
constexpr auto first_layer_size = 70;
constexpr auto second_layer_size = 63;
constexpr auto output_layer_size = 2;
constexpr auto epoch_count = 29500;
constexpr auto learning_rate = 0.03;
constexpr auto low_weight = -1.9;
constexpr auto high_weight = 1.9;

auto getDS(const std::string& filename) -> DataSet
{
    auto ifs = std::ifstream(filename);
    auto trainDatum = parse_csv(ifs, ',', 2);
    fixNanDataSet(trainDatum);
    normalInput(trainDatum, input_size);
    normalOutput(trainDatum, output_layer_size);

    return trainDatum;
}

int main()
{
    auto trainDatum = getDS("../newTrainSet.csv");

    auto percteptron = std::make_unique<Perceptron>(input_size, first_layer_size, second_layer_size, output_layer_size);
    percteptron->initWeights(low_weight, high_weight);
    auto trainMseVector = percteptron->train(trainDatum, epoch_count, learning_rate);

    auto testDatum = getDS("../newTestSet.csv");
    auto resDev = percteptron->getTestResult(testDatum);

    saveError(trainMseVector, "trainMse.txt");
    saveTestErrors(resDev);

    return 0;
}
