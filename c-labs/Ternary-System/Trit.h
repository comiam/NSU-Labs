#pragma once
#include <iostream>

enum class trit: short {
    False = 0,//-1
    Unknown = 1,//0
    True = 3//1
};

trit operator!(trit a);
trit operator|(trit a, trit b);
trit operator&(trit a, trit b);
std::ostream& operator<<(std::ostream& os, trit a);