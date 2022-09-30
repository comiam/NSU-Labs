#ifndef SEABATTLE_JUDGE_H
#define SEABATTLE_JUDGE_H

#include "Player.h"

class Judge
{
protected:
    vector<Ship> ships0;
    vector<Ship> ships1;
    int findShipByCoord(int x, int y, bool first);
public:
    void setShips(vector<Ship>& shipsFirst, vector<Ship>& shipsSecond);
    bool playerIsDead(bool first);
    bool playerFire(bool first, vector<vector<int>>& field, Player* player);
    static bool fillShips(vector<vector<int>>& field, vector<Ship>& ships);
    static bool checkShipSet(vector<Ship>& shipsFirst);
};


#endif
