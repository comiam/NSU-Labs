#ifndef SEABATTLE_SHIPS_H
#define SEABATTLE_SHIPS_H

#include <vector>
#include <cstdlib>

#define DAMAGED -2
#define FIRED -1
#define EMPTY_FIELD 0
#define PLAYER_SHIP 1

using namespace std;

enum ShipType
{
    carrier,
    cruiser,
    destroyer,
    boat,
    unknown
};

int getLength(ShipType type);

typedef struct ship
{
    ship(ShipType type, int x, int y, bool horizontal)
    {
        this->type = type;
        this->x = x;
        this->y = y;
        health = getLength(type);
        this->horizontal = horizontal;
    }
    ShipType type = unknown;
    int x = -1;
    int y = -1;
    bool horizontal  = false;

    friend class Judge;
private:
    bool destroyed = false;
    int health = -1;
} Ship;

void surroundShip(vector<vector<int>>& field, Ship ship);
vector<Ship> randomShips();
bool setShip(vector<vector<int>>& field, int x, int y, bool horizontal, ShipType type);
bool goodShip(vector<vector<int>>& field, int x, int y, bool horizontal, ShipType type);

#endif