#ifndef SEABATTLE_GAMECORE_H
#define SEABATTLE_GAMECORE_H

enum PlayerType
{
    Random,
    Optional,
    Interactive
};

void run(int rounds, PlayerType firstType, PlayerType secondType);

#endif