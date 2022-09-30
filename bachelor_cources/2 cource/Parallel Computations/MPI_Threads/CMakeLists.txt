cmake_minimum_required(VERSION 3.16)
project(MPI_Threads)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_COMPILER "/usr/bin/mpicc")
set(CMAKE_CXX_COMPILER "/usr/bin/mpic++")
set(CMAKE_CXX_FLAGS "-O3 -pthread")

add_executable(MPI_Threads main.cpp list.cpp list.h threads.cpp threads.h messages.cpp messages.h)