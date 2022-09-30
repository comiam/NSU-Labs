cmake_minimum_required(VERSION 3.17)
project(shannon)

set(CMAKE_CXX_STANDARD 11)

add_executable(shannon main.cpp utils.h utils.cpp tree.h tree.cpp encoder.cpp encoder.h decoder.h decoder.cpp)