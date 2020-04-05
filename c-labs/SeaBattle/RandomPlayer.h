#ifndef SEABATTLE_RANDOMPLAYER_H
#define SEABATTLE_RANDOMPLAYER_H

#include "Player.h"

class RandomPlayer: public Player
{
public:
    vector<Ship> createShips() override;
    pair<int,int> nextStep() override;
    void update() override {};
};


#endif
