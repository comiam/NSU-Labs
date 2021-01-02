cmake_minimum_required(VERSION 3.17)
project(1_lab C)

set(CMAKE_C_STANDARD 11)
find_package(Threads REQUIRED)

add_executable(1_lab main.c)
target_link_libraries(${PROJECT_NAME} Threads::Threads)