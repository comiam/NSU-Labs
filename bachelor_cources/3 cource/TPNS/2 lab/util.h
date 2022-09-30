#ifndef PERCEPTRON_UTIL_H
#define PERCEPTRON_UTIL_H

#include "Perceptron.h"

using NdArrayF = nc::NdArray<float>;
using Data = std::pair<NdArrayF, NdArrayF>;
using DataSet = std::vector<Data>;

inline float sigmoid(float x)
{
    return 1.f / (1.f + std::exp(-x));
}

inline float mse(const nc::NdArray<float> &error)
{
    auto mse = 0.f;
    auto len = error.size();
    for (auto i = 0; i < len; ++i)
        mse += (error[i] * error[i]);

    return mse;
}

void fixNanDataSet(DataSet &datum)
{
    for (auto &i : datum)
        for (float &j : i.first)
            j = std::isnan(j) ? 0.f : j;
}

void normalOutput(DataSet &datum, unsigned int output_layer_size)
{
    NdArrayF min = nc::zeros<float>({1, output_layer_size});
    NdArrayF max = nc::zeros<float>({1, output_layer_size});
    for (auto i = 0; i < output_layer_size; ++i)
    {
        max[i] = std::numeric_limits<float>::lowest();
        min[i] = std::numeric_limits<float>::max();
    }

    for (auto &i : datum)
        for (auto j = 0; j < i.second.size(); ++j)
            if (!std::isnan(i.second[j]))
            {
                max[j] = max[j] < i.second[j] ? i.second[j] : max[j];
                min[j] = min[j] > i.second[j] ? i.second[j] : min[j];
            }

    for (auto &i : datum)
        for (auto j = 0; j < i.second.size(); ++j)
            if (!std::isnan(i.second[j]))
                i.second[j] = (i.second[j] - min[j]) / (max[j] - min[j]);
}

void normalInput(DataSet &datum, unsigned int input_size)
{
    NdArrayF min = nc::zeros<float>({1, input_size});
    NdArrayF max = nc::zeros<float>({1, input_size});
    for (auto i = 0; i < input_size; ++i)
    {
        max[i] = std::numeric_limits<float>::lowest();
        min[i] = std::numeric_limits<float>::max();
    }

    for (auto &i : datum)
        for (auto j = 0; j < i.first.size(); ++j)
            if (!std::isnan(i.first[j]))
            {
                max[j] = max[j] < i.first[j] ? i.first[j] : max[j];
                min[j] = min[j] > i.first[j] ? i.first[j] : min[j];

            }

    for (auto &i : datum)
        for (auto j = 0; j < i.first.size(); ++j)
            if (!std::isnan(i.first[j]))
                i.first[j] = (i.first[j] - min[j]) / (max[j] - min[j]);
}

#endif
