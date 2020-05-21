cmake_minimum_required(VERSION 3.14)
project(CSVParser)

set(CMAKE_CXX_STANDARD 17)

add_executable(CSVParser main.cpp TupleUtils.h CSVParser.h)