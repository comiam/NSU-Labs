#include "Judge.h"

void Judge::setShips(vector<Ship> &shipsFirst, vector<Ship> &shipsSecond)
{
    for(Ship a : shipsFirst)
        a.destroyed = false;

    for(Ship b : shipsSecond)
        b.destroyed = false;

    this->ships0 = shipsFirst;
    this->ships1 = shipsSecond;
}

int Judge::findShipByCoord(int x, int y, bool first)
{
    for(int i = 0; i < (first ? ships0 : ships1).size(); i++)
    {
        Ship a = (first ? ships0 : ships1)[i];
        if(a.horizontal && y == a.y && a.x + getLength(a.type) - 1 >= x)
        {
            for(int j = 0; j < getLength(a.type); ++j)
                if(a.x + j == x)
                    return i;
        } else if(!a.horizontal && x == a.x)
        {
            for(int j = 0; j < getLength(a.type); ++j)
                if(a.y + j == y)
                    return i;
        }
    }
    return -1;
}

bool Judge::playerFire(bool first, vector<vector<int>> &field, Player *player)
{
    while(true)
    {
        pair<int, int> coord = player->nextStep();
        if(coord.first < 0 || coord.first > 9 || coord.second < 0 || coord.second > 9)
        {
            player->successDestruction(false);
            player->successFire(false);
            continue;
        }

        switch(field[coord.second][coord.first])
        {
            case EMPTY_FIELD:
                field[coord.second][coord.first] = FIRED;
                player->successDestruction(false);
                player->successFire(false);
                return false;
            case DAMAGED:
            case FIRED:
                player->successDestruction(false);
                player->successFire(false);
                continue;
            case PLAYER_SHIP:
                int i = findShipByCoord(coord.first, coord.second, !first);

                player->successDestruction(false);
                player->successFire(true);

                Ship& a = (first ? ships1 : ships0)[i];

                if(a.health <= 1)
                {
                    player->successDestruction(true);
                    a.destroyed = true;
                    surroundShip(field, a);
                }

                a.health--;

                field[coord.second][coord.first] = DAMAGED;
                return true;
        }
    }
}

bool Judge::fillShips(vector<vector<int>> &field, vector<Ship> &ships)
{
    for(Ship a : ships)
        if(!goodShip(field, a.x, a.y, a.horizontal, a.type) || !setShip(field, a.x, a.y, a.horizontal, a.type))
            return false;
    return true;
}

bool Judge::checkShipSet(vector<Ship> &shipsFirst)
{
    int carriers = 0;
    int cruisers = 0;
    int destroyers = 0;
    int boats = 0;

    for(auto &j : shipsFirst)
        switch(j.type)
        {
            case carrier:
                carriers++;
                break;
            case cruiser:
                cruisers++;
                break;
            case destroyer:
                destroyers++;
                break;
            case boat:
                boats++;
                break;
            case unknown:
                return false;
        }

    return carriers == 1 && cruisers == 2 && destroyers == 3 && boats == 4;
}

bool Judge::playerIsDead(bool first)
{
    if(first)
    {
        for(Ship ship : ships0)
            if(!ship.destroyed)
                return false;
    } else
    {
        for(Ship ship : ships1)
            if(!ship.destroyed)
                return false;
    }
    return true;
}
