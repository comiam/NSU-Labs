#include "RandomPlayer.h"

pair<int, int> RandomPlayer::nextStep()
{
    int x = rand() % 10;
    int y = rand() % 10;

    return make_pair(x,y);
}

vector<Ship> RandomPlayer::createShips()
{
    return randomShips();
}
