#ifndef SEABATTLE_PLAYER_H
#define SEABATTLE_PLAYER_H

#include "GameCore.h"
#include "Ships.h"
#include <vector>
#include <ctime>
#include <cstdlib>
#include <iostream>

using namespace std;

class Player
{
protected:
    bool successOnLastStep = false;
    bool destroyShipOnLast = false;
public:
    virtual vector<Ship> createShips() = 0;
    virtual pair<int,int> nextStep() = 0;
    virtual void update() = 0;
    void successFire(bool success)
    {
        successOnLastStep = success;
    }
    void successDestruction(bool success)
    {
        destroyShipOnLast = success;
    }

    static Player *createPlayer(PlayerType playerType);
};

#endif
