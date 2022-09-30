cmake_minimum_required(VERSION 3.17)
project(perceptron)

set(CMAKE_CXX_STANDARD 20)
include_directories(noBoostNumCpp)
add_executable(perceptron main.cpp Perceptron.h Layer.h util.h io.h)