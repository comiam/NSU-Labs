#include "Player.h"
#include "RandomPlayer.h"
#include "OptionalPlayer.h"
#include "ConsolePlayer.h"

Player *Player::createPlayer(PlayerType playerType)
{
    Player *gamer = nullptr;
    switch(playerType)
    {
        case Random:
            gamer = new RandomPlayer();
            break;
        case Optional:
            gamer = new OptionalPlayer();
            break;
        case Interactive:
            gamer = new ConsolePlayer();
            break;
    }
    return gamer;
}
