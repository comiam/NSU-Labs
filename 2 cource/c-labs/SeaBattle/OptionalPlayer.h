#ifndef SEABATTLE_OPTIONALPLAYER_H
#define SEABATTLE_OPTIONALPLAYER_H
#include "Player.h"

class OptionalPlayer:public Player
{
private:
    int vectorFire = 0;
    vector<pair<int,int>> lastSuccessPoints;
    pair<int,int>   lastPoint;
    int diag0 = 0;
    int diag1 = 0;
    int diag2 = 0;
    int diag3 = 0;
    pair<int, int> strategyFire();
    int fullCycle = 0;
public:
    vector<Ship> createShips() override;
    pair<int,int> nextStep() override;
    void update() override;
};


#endif
