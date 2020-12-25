cmake_minimum_required(VERSION 3.17)
project(multithread)

set(CMAKE_CXX_STANDARD 11)

add_executable(multithread main.cpp Cache.h Cache.cpp http_parser.c http_parser.h ConnectionHandler.h ProxyCore.cpp ProxyCore.h Client.cpp Client.h Server.cpp Server.h Monitor.h Monitor.cpp errhandle.h)

target_link_libraries(multithread "pthread")