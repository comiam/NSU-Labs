cmake_minimum_required(VERSION 3.15)
project(diffurLab)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_COMPILER "/usr/bin/mpicc")
set(CMAKE_CXX_COMPILER "/usr/bin/mpic++")
set(CMAKE_CXX_FLAGS "-O3")

add_executable(diffurLab main.cpp)