cmake_minimum_required(VERSION 3.15)
project(seabattle)

set(CMAKE_CXX_STANDARD 17)

add_executable(seabattle main.cpp GameCore.cpp GameCore.h GameView.cpp GameView.h Player.h ConsolePlayer.cpp ConsolePlayer.h RandomPlayer.cpp RandomPlayer.h OptionalPlayer.cpp OptionalPlayer.h Player.cpp Ships.cpp Ships.h Judge.cpp Judge.h)