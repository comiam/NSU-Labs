cmake_minimum_required(VERSION 3.13)
project(huffman C)

set(CMAKE_C_STANDARD 11)

add_executable(huffman main.c encoder.c encoder.h tree.c tree.h utils.c utils.h decoder.c decoder.h)