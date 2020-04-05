cmake_minimum_required(VERSION 3.14)
project(TritNSystem)

set(CMAKE_CXX_STANDARD 17)

################################
# GTest
################################
ADD_SUBDIRECTORY (googletest)
enable_testing()
include_directories(googletest/include/gtest)

################################
# Unit Tests
################################
# Add test cpp file
add_executable(TritNSystem Trit.cpp Trit.h TritSet.cpp TritSet.h tests.cpp)
# Link test executable against gtest & gtest_main
target_link_libraries(TritNSystem gtest gtest_main)
add_test(TritNSystem TritNSystem)

#add_executable(TritNSystem main.cpp Trit.cpp Trit.h TritSet.cpp TritSet.h)