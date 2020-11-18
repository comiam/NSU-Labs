cmake_minimum_required(VERSION 3.17)
project(singlethread)

set(CMAKE_CXX_STANDARD 11)

add_executable(singlethread main.cpp Cache.h Cache.cpp http_parser.c http_parser.h ConnectionHandler.h ProxyCore.cpp ProxyCore.h Client.cpp Client.h Server.cpp Server.h)