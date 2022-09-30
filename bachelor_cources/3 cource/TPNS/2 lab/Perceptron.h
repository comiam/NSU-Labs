#ifndef PERCEPTRON_H
#define PERCEPTRON_H

#include "Layer.h"

using Data = std::pair<NdArrayF, NdArrayF>;
using DataSet = std::vector<Data>;

class Perceptron
{
private:
    NdArrayF inputLayer;
    Layer firstLayer;
    Layer secondLayer;
    Layer outputLayer;

    NdArrayF calcOutputDelta(const NdArrayF &error)
    {
        return outputLayer.getLayerOutput() * (1.f - outputLayer.getLayerOutput()) * error;
    }

    NdArrayF calcHiddenDelta(const NdArrayF &nextLayerDelta, const NdArrayF &nextLayerInput,
                             const NdArrayF &nextLayerWeights)
    {
        auto curDelta = nc::matmul(nextLayerDelta, nextLayerWeights.transpose());
        return nextLayerInput * (1.f - nextLayerInput) * curDelta;
    }

    NdArrayF calcWeightCorr(const NdArrayF &layerDelta, const NdArrayF &layerInput)
    {
        return nc::matmul(layerInput.transpose(), layerDelta);
    }


public:
    Perceptron(unsigned inputLayerSize, unsigned firstLayerSize, unsigned secondLayerSize, unsigned outputLayerSize) :
            firstLayer(firstLayerSize, inputLayerSize), secondLayer(secondLayerSize, firstLayerSize),
            outputLayer(outputLayerSize, secondLayerSize)
    {
        inputLayer = nc::zeros<float>({1, inputLayerSize});
    }

    void initWeights(float low, float high)
    {
        firstLayer.initWeights(low, high);
        secondLayer.initWeights(low, high);
        outputLayer.initWeights(low, high);
    }

    void feedForward(const NdArrayF &input)
    {
        inputLayer = input;
        firstLayer.activateNeurones(inputLayer);
        secondLayer.activateNeurones(firstLayer.getLayerOutput());
        outputLayer.activateNeurones(secondLayer.getLayerOutput());
    }

    void backProp(const NdArrayF &error, float lr)
    {
        auto outputLayerDelta = calcOutputDelta(error);
        auto outputLayerWeightCorr = calcWeightCorr(outputLayerDelta, secondLayer.getLayerOutput());
        auto secondLayerDelta = calcHiddenDelta(outputLayerDelta, secondLayer.getLayerOutput(),
                                                outputLayer.getLayerWeights());
        auto secondLayerWeightCorr = calcWeightCorr(secondLayerDelta, firstLayer.getLayerOutput());
        auto firstLayerDelta = calcHiddenDelta(secondLayerDelta, firstLayer.getLayerOutput(),
                                               secondLayer.getLayerWeights());
        auto firstLayerWeightCorr = calcWeightCorr(firstLayerDelta, inputLayer);
        firstLayer.setLayerWeights(firstLayer.getLayerWeights() - lr * firstLayerWeightCorr);
        secondLayer.setLayerWeights(secondLayer.getLayerWeights() - lr * secondLayerWeightCorr);
        outputLayer.setLayerOutput(outputLayer.getLayerWeights() - lr * outputLayerWeightCorr);
    }

    NdArrayF train(const DataSet &dataSet, unsigned epochCount, float lr)
    {
        auto iterations = dataSet.size();
        auto mseVector = nc::zeros<float>({1, epochCount});
        for (auto epoch = 0; epoch < epochCount; ++epoch)
        {

            auto avgMse = 0.f;
            for (auto i = 0; i < iterations; ++i)
            {
                feedForward(dataSet[i].first);
                auto error = outputLayer.getLayerOutput() - dataSet[i].second;
                fixNanError(error);
                backProp(error, lr);
                avgMse += mse(error);

            }
            mseVector[epoch] = avgMse / iterations;
            std::cout << "epoch: " << epoch << " MSE: " << mseVector[epoch] << std::endl;
        }
        return mseVector;
    }

    void fixNanError(NdArrayF &error)
    {
        for (auto i = 0; i < error.size(); ++i)
            error[i] = std::isnan(error[i]) ? 0.f : error[i];
    }


    std::pair<std::vector<NdArrayF>, std::vector<float>> getTestResult(const DataSet &dataSet)
    {
        auto iterations = dataSet.size();
        auto errorList = std::vector<NdArrayF>();
        auto mseError = std::vector<float>();
        for (auto i = 0; i < iterations; ++i)
        {
            std::cout << "iter: " << i + 1 << std::endl;
            std::cout << "input: " << dataSet[i].first << " ";
            feedForward(dataSet[i].first);
            std::cout << "output: " << outputLayer.getLayerOutput() << " ";
            std::cout << "real output: " << dataSet[i].second << std::endl;
            auto error = outputLayer.getLayerOutput() - dataSet[i].second;
            fixNanError(error);
            std::cout << "deviation: " << error << std::endl;
            errorList.emplace_back(error);
            mseError.emplace_back(mse(error));
        }
        std::pair<std::vector<NdArrayF>, std::vector<float>> pair{};
        pair.first = errorList;
        pair.second = mseError;
        return pair;
    }
};


#endif
