#include "Ships.h"

int getLength(ShipType type)
{
    switch(type)
    {
        case carrier:
            return 4;
        case cruiser:
            return 3;
        case destroyer:
            return 2;
        case boat:
            return 1;
        case unknown:
            return 0;
    }
    return -1;
}

vector<Ship> randomShips()
{
    vector<vector<int>> field;
    vector<Ship> ships;
    for(int i = 0; i < 10; ++i)
    {
        vector<int> row(10, 0);
        field.push_back(row);
    }

    bool horizontal;
    int x, y;
    do
    {
        horizontal = (rand() % 2);
        x = rand() % 10;
        y = rand() % 10;
    } while(!goodShip(field, x, y, horizontal, ShipType::carrier));
    setShip(field, x, y, horizontal, ShipType::carrier);
    {
        Ship a(ShipType::carrier, x, y, horizontal);
        ships.emplace_back(a);
    }
    for(int i = 0; i < 2; ++i)
    {
        do
        {
            horizontal = (rand() % 2);
            x = rand() % 10;
            y = rand() % 10;
        } while(!goodShip(field, x, y, horizontal, ShipType::cruiser));
        setShip(field, x, y, horizontal, ShipType::cruiser);
        {
            Ship a(ShipType::cruiser, x, y, horizontal);
            ships.emplace_back(a);
        }
    }
    for(int i = 0; i < 3; ++i)
    {
        do
        {
            horizontal = (rand() % 2);
            x = rand() % 10;
            y = rand() % 10;
        } while(!goodShip(field, x, y, horizontal, ShipType::destroyer));
        setShip(field, x, y, horizontal, ShipType::destroyer);
        {
            Ship a(ShipType::destroyer, x, y, horizontal);
            ships.emplace_back(a);
        }
    }
    for(int i = 0; i < 4; ++i)
    {
        do
        {
            horizontal = (rand() % 2);
            x = rand() % 10;
            y = rand() % 10;
        } while(!goodShip(field, x, y, horizontal, ShipType::boat));
        setShip(field, x, y, horizontal, ShipType::boat);
        {
            Ship a(ShipType::boat, x, y, horizontal);
            ships.emplace_back(a);
        }
    }
    return ships;
}

void surroundShip(vector<vector<int>> &field, Ship ship)
{
    int length = getLength(ship.type);
    int x = ship.x, y = ship.y;

    for(int i = 0; i < length; ++i)
    {
        if(x - 1 > -1 && field[y][x - 1] == EMPTY_FIELD)
            field[y][x - 1] = FIRED;

        if(x + 1 < 10 && field[y][x + 1] == EMPTY_FIELD)
            field[y][x + 1] = FIRED;

        if(x - 1 > -1 && y - 1 > -1 && field[y - 1][x - 1] == EMPTY_FIELD)
            field[y - 1][x - 1] = FIRED;

        if(x - 1 > -1 && y + 1 < 10 && field[y + 1][x - 1] == EMPTY_FIELD)
            field[y + 1][x - 1] = FIRED;

        if(x + 1 < 10 && y - 1 > -1 && field[y - 1][x + 1] == EMPTY_FIELD)
            field[y - 1][x + 1] = FIRED;

        if(y + 1 < 10 && field[y + 1][x] == EMPTY_FIELD)
            field[y + 1][x] = FIRED;

        if(y - 1 > -1 && field[y - 1][x] == EMPTY_FIELD)
            field[y - 1][x] = FIRED;

        if(x + 1 < 10 && y + 1 < 10 && field[y + 1][x + 1] == EMPTY_FIELD)
            field[y + 1][x + 1] = FIRED;

        if(ship.horizontal)
            x += 1;
        else
            y += 1;
    }
}

bool setShip(vector<vector<int>> &field, int x, int y, bool horizontal, ShipType type0)
{
    int length = getLength(type0);

    if(horizontal)
        for(int i = 0; i < length; ++i)
            if(field[y][x + i] == PLAYER_SHIP)
                return false;
            else
                field[y][x + i] = PLAYER_SHIP;
    else
        for(int i = 0; i < length; ++i)
            if(field[y + i][x] == PLAYER_SHIP)
                return false;
            else
                field[y + i][x] = PLAYER_SHIP;

    return true;
}

bool goodShip(vector<vector<int>> &field, int x, int y, bool horizontal, ShipType type0)
{
    int length = getLength(type0);

    if(horizontal)
    {
        if(!(x >= 0 && x + length - 1 < 10) || !(y >= 0 && y <= 9))
            return false;
        else
            for(int i = 0; i < length; ++i)
            {
                if(field[y][x + i] == PLAYER_SHIP)
                    return false;
                if(x + i - 1 > -1 && field[y][x + i - 1] == PLAYER_SHIP)
                    return false;

                if(x + i + 1 < 10 && field[y][x + i + 1] == PLAYER_SHIP)
                    return false;

                if(x + i - 1 > -1 && y - 1 > -1 && field[y - 1][x + i - 1] == PLAYER_SHIP)
                    return false;

                if(x + i - 1 > -1 && y + 1 < 10 && field[y + 1][x + i - 1] == PLAYER_SHIP)
                    return false;

                if(x + i + 1 < 10 && y - 1 > -1 && field[y - 1][x + i + 1] == PLAYER_SHIP)
                    return false;

                if(y + i + 1 < 10 && field[y + 1][x + i] == PLAYER_SHIP)
                    return false;

                if(y - 1 > -1 && field[y - 1][x + i] == PLAYER_SHIP)
                    return false;

                if(x + i + 1 < 10 && y + 1 < 10 && field[y + 1][x + i + 1] == PLAYER_SHIP)
                    return false;
            }
    } else
    {
        if(!(y >= 0 && y + length - 1 < 10) || !(x >= 0 && x <= 9))
            return false;
        else
            for(int i = 0; i < length; ++i)
            {
                if(field[y + i][x] > 0)
                    return false;
                if(x - 1 > -1 && field[y + i][x - 1] == PLAYER_SHIP)
                    return false;

                if(x + 1 < 10 && field[y + i][x + 1] == PLAYER_SHIP)
                    return false;

                if(x - 1 > -1 && y + i - 1 > -1 && field[y + i - 1][x - 1] == PLAYER_SHIP)
                    return false;

                if(x - 1 > -1 && y + i + 1 < 10 && field[y + i + 1][x - 1] == PLAYER_SHIP)
                    return false;

                if(x + 1 < 10 && y + i - 1 > -1 && field[y + i - 1][x + 1] == PLAYER_SHIP)
                    return false;

                if(y + i + 1 < 10 && field[y + i + 1][x] == PLAYER_SHIP)
                    return false;

                if(y + i - 1 > -1 && field[y + i - 1][x] == PLAYER_SHIP)
                    return false;

                if(x + 1 < 10 && y + i + 1 < 10 && field[y + i + 1][x + 1] == PLAYER_SHIP)
                    return false;
            }
    }
    return true;
}