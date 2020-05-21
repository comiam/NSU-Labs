#ifndef SEABATTLE_CONSOLEPLAYER_H
#define SEABATTLE_CONSOLEPLAYER_H
#include "Player.h"

class ConsolePlayer: public Player
{
public:
    vector<Ship> createShips() override;
    pair<int,int> nextStep() override;
    void update() override {};
};

#endif
