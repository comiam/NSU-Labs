#include <vector>
#include <iostream>

#include "GameCore.h"
#include "Player.h"
#include "GameView.h"
#include "Judge.h"

using namespace std;

void playGame(vector<vector<int>> &fieldFirst, vector<vector<int>> &fieldSecond, Player *first, Player *second,
              PlayerType firstType, PlayerType secondType, Judge &judge, int round, int score0, int score1, bool delay, bool showStats);
pair<bool, string>
updateGame(vector<vector<int>> &fieldFirst, vector<vector<int>> &fieldSecond, Player *first, Player *second,
           Judge &judge);

void run(int rounds, PlayerType firstType, PlayerType secondType)
{
    createWindow(750, 330);
    moveCenter();

    srand(time(nullptr));

    Player *first = Player::createPlayer(firstType);
    Player *second = Player::createPlayer(secondType);

    vector<vector<int>> fieldFirst;
    vector<vector<int>> fieldSecond;

    Judge judge;

    int scoreFirst = 0;
    int scoreSecond = 0;

    bool delay = false;
    if(firstType != PlayerType::Interactive && secondType != PlayerType::Interactive)
        delay = ask("Make a delay between computer moves? y/n");

    for(int i = 0; i < rounds; ++i)
    {
        pair<bool, string> res = updateGame(fieldFirst, fieldSecond, first, second, judge);
        if(!res.first)
        {
            cout << res.second << endl;
            free(first);
            free(second);
            return;
        }
        playGame(fieldFirst, fieldSecond, first, second, firstType, secondType, judge, i + 1, scoreFirst, scoreSecond,
                 delay, rounds != 1);

        if(rounds != 1 && ((firstType == PlayerType::Interactive && secondType == PlayerType::Interactive) || delay))
            showWinner0(judge.playerIsDead(true));

        scoreFirst += judge.playerIsDead(false);
        scoreSecond += judge.playerIsDead(true);
        showGame(fieldFirst, fieldSecond, false, false, i + 1, scoreFirst, scoreSecond, rounds != 1);
        if((firstType == PlayerType::Interactive && secondType == PlayerType::Interactive) || delay)
            Sleep(3000);
        else
            Sleep(100);
    }

    if(scoreFirst == scoreSecond)
    {
        if((firstType == PlayerType::Interactive && secondType == PlayerType::Interactive) || delay)
        {
            viewMessage("Players have equal scores, there will be a new round!");
            Sleep(3000);
        } else
            Sleep(10);

        pair<bool, string> res = updateGame(fieldFirst, fieldSecond, first, second, judge);
        if(!res.first)
        {
            cout << res.second << endl;
            free(first);
            free(second);
            return;
        }
        playGame(fieldFirst, fieldSecond, first, second, firstType, secondType, judge, rounds + 1, scoreFirst,
                 scoreSecond, delay, rounds != 1);

        scoreFirst += !judge.playerIsDead(true);
        scoreSecond += judge.playerIsDead(true);
        rounds++;
    }
    showGame(fieldFirst, fieldSecond, false, false, rounds, scoreFirst, scoreSecond, rounds != 1);
    system("pause");
    showWinner1(scoreFirst > scoreSecond);
    system("pause");

    free(first);
    free(second);
}

void playGame(vector<vector<int>> &fieldFirst, vector<vector<int>> &fieldSecond, Player *first, Player *second,
              PlayerType firstType, PlayerType secondType, Judge &judge, int round, int score0, int score1, bool delay, bool showStats)
{
    bool moveFirst = (rand() % 2);

    bool successFire;
    while(!judge.playerIsDead(true) && !judge.playerIsDead(false))
    {
        do
        {
            if(firstType == PlayerType::Interactive && secondType == PlayerType::Interactive)
                showGame(fieldFirst, fieldSecond, !moveFirst, moveFirst, round, score0, score1, showStats);
            else if(firstType != PlayerType::Interactive && secondType != PlayerType::Interactive)
                showGame(fieldFirst, fieldSecond, false, false, round, score0, score1, showStats);
            else
                showGame(fieldFirst, fieldSecond, firstType != PlayerType::Interactive,
                         secondType != PlayerType::Interactive, round, score0, score1, showStats);

            if(delay)
                Sleep(100);

            successFire = judge.playerFire(moveFirst, moveFirst ? fieldSecond : fieldFirst, moveFirst ? first : second);
            (moveFirst ? first : second)->successFire(successFire);
        } while(successFire && !judge.playerIsDead(true) && !judge.playerIsDead(false));

        moveFirst = !moveFirst;
    }
}

pair<bool, string>
updateGame(vector<vector<int>> &fieldFirst, vector<vector<int>> &fieldSecond, Player *first, Player *second,
           Judge &judge)
{
    first->update();
    second->update();

    fieldFirst.clear();
    fieldSecond.clear();
    for(int i = 0; i < 10; ++i)
    {
        vector<int> row(10, EMPTY_FIELD);
        vector<int> row0(10, EMPTY_FIELD);
        fieldFirst.push_back(row);
        fieldSecond.push_back(row0);
    }
    vector<Ship> a(first->createShips());
    vector<Ship> b(second->createShips());

    if(!Judge::checkShipSet(a))
        return make_pair<bool, string>(false, "bad ships set of first player!");

    if(!Judge::checkShipSet(b))
        return make_pair<bool, string>(false, "bad ships set of second player!");

    judge.setShips(a, b);
    if(!Judge::fillShips(fieldFirst, a))
        return make_pair<bool, string>(false, "bad ships set of first player!");

    if(!Judge::fillShips(fieldSecond, b))
        return make_pair<bool, string>(false, "bad ships set of second player!");

    return make_pair(true, "");
}
