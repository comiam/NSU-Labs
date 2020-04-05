#include "OptionalPlayer.h"

vector<Ship> OptionalPlayer::createShips()
{
    return randomShips();
}

pair<int, int> OptionalPlayer::strategyFire()
{
    fullCycle = 0;
    if(diag0 < 10)
    {
        lastPoint = make_pair(diag0,diag0);
        diag0++;
        return lastPoint;
    } else if(diag1 < 10)
    {
        lastPoint = make_pair(9 - diag1,diag1);
        diag1++;
        return lastPoint;
    } else if(diag2 < 10)
    {
        lastPoint = make_pair(4,diag2);
        diag2++;
        return lastPoint;
    } else if(diag3 < 10)
    {
        lastPoint = make_pair(diag3,5);
        diag3++;
        return lastPoint;
    } else
    {
        lastPoint = make_pair(rand() % 10, rand() % 10);
        return lastPoint;
    }
}

pair<int, int> OptionalPlayer::nextStep()
{
    if(!successOnLastStep && lastSuccessPoints.empty())
        return strategyFire();
    else if(!successOnLastStep && !lastSuccessPoints.empty())
    {
        vectorFire = (vectorFire + 1) % 4;
        fullCycle++;
    }
    else if(destroyShipOnLast)
    {
        fullCycle = 0;
        lastSuccessPoints.clear();
        return strategyFire();
    }
    else if(successOnLastStep)
        lastSuccessPoints.push_back(lastPoint);

    switch(vectorFire)
    {
        case 0:
            lastPoint = make_pair(lastSuccessPoints[lastSuccessPoints.size() - 1].first + 1, lastSuccessPoints[lastSuccessPoints.size() - 1].second);
            break;
        case 1:
            lastPoint = make_pair(lastSuccessPoints[lastSuccessPoints.size() - 1].first - 1, lastSuccessPoints[lastSuccessPoints.size() - 1].second);
            break;
        case 2:
            lastPoint = make_pair(lastSuccessPoints[lastSuccessPoints.size() - 1].first, lastSuccessPoints[lastSuccessPoints.size() - 1].second + 1);
            break;
        case 3:
            lastPoint = make_pair(lastSuccessPoints[lastSuccessPoints.size() - 1].first, lastSuccessPoints[lastSuccessPoints.size() - 1].second - 1);
            break;
    }
    if(fullCycle > 4)
    {
        lastSuccessPoints.pop_back();
        fullCycle = 0;
    }

    return lastPoint;
}

void OptionalPlayer::update()
{
    lastSuccessPoints.clear();
    fullCycle = 0;
    diag0 = 0;
    diag1 = 0;
    diag2 = 0;
    diag3 = 0;
    lastPoint = make_pair<int>(0,0);
    vectorFire = 0;
    successOnLastStep = false;
    destroyShipOnLast = false;
}
