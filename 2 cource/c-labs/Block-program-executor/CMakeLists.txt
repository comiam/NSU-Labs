cmake_minimum_required(VERSION 3.15)
project(BlockExec)

set(CMAKE_CXX_STANDARD 17)

add_executable(BlockExec main.cpp Worker.h FileWriterBlock.cpp FileWriterBlock.h GrepBlock.cpp GrepBlock.h SortBlock.cpp SortBlock.h ReplaceBlock.cpp ReplaceBlock.h DumpBlock.cpp DumpBlock.h ProgramParser.cpp ProgramParser.h Validator.cpp Validator.h BlockProgram.cpp BlockProgram.h FileReaderBlock.cpp FileReaderBlock.h)