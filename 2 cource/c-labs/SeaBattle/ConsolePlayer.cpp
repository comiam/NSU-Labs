#include "ConsolePlayer.h"
#include "Judge.h"
#include "GameView.h"

pair<int, int> ConsolePlayer::nextStep()
{
    cout << "Input <x(int)> <y(int)>" << endl;

    int x = 0, y = 0;
    while(true)
    {
        cin >> x;
        if(!cin || !(x > 0 && x < 11))
        {
            cout << "Wrong coords, repeat plz" << endl;
            continue;
        }
        cin >> y;
        if(!cin || !(y > 0 && y < 11))
        {
            cout << "Wrong coords, repeat plz" << endl;
            continue;
        }
        return make_pair(x - 1,y - 1);
    }
}

void printUsage(vector<vector<int>>& field)
{
    showFieldEditor(field);

    cout << "Input <x(int)> <y(int)> <horizontal(y/n)> <typeShip(int)>  until you collect the field!" << endl;
    cout << "typeShip:" << endl;
    cout << "0 - carrier" << endl;
    cout << "1 - cruiser" << endl;
    cout << "2 - destroyer" << endl;
    cout << "3 - boat" << endl << endl;
}

vector<Ship> ConsolePlayer::createShips()
{
    char ans = 0;

    while(tolower(ans, std::locale()) != 'y' && tolower(ans, std::locale()) != 'n')
    {
        cout << "Do you want to set the ships yourself? y/n" << endl;
        ans = getchar();
    }

    if(tolower(ans, std::locale()) == 'n')
        return randomShips();
    else
    {
        int carriers = 0;
        int cruisers = 0;
        int destroyers = 0;
        int boats = 0;

        vector<vector<int>> field;
        vector<Ship> ships;

        for(int i = 0; i < 10; ++i)
        {
            vector<int> row(10, EMPTY_FIELD);
            field.push_back(row);
        }
        printUsage(field);

        int x = 0, y = 0, shipType = 0;
        char hor = 0;
        ShipType type = destroyer;

        while(true)
        {
            cin >> x;
            if(!cin)
            {
                cout << "Wrong string, repeat plz" << endl;
                continue;
            }
            cin >> y;
            if(!cin)
            {
                cout << "Wrong string, repeat plz" << endl;
                continue;
            }
            cin >> hor;
            if(!cin)
            {
                cout << "Wrong string, repeat plz" << endl;
                continue;
            }
            cin >> shipType;
            if(!cin)
            {
                cout << "Wrong string, repeat plz" << endl;
                continue;
            }

            if(!(x > 0 && x < 11) || !(y > 0 && y < 11) || !(tolower(hor, std::locale()) == 'y'
                                                               || tolower(hor, std::locale()) == 'n') ||
                                                                        !(shipType > -1 && shipType < 4))
            {
                cout << "Wrong string, repeat plz" << endl;
                continue;
            }

            switch(shipType)
            {
                case 0:
                    if(carriers == 1)
                    {
                        cout << "You already have a carrier!" << endl;
                        continue;
                    }
                    type = ShipType::carrier;
                    break;
                case 1:
                    if(cruisers == 2)
                    {
                        cout << "You already have a cruisers!" << endl;
                        continue;
                    }
                    type = ShipType::cruiser;
                    break;
                case 2:
                    if(destroyers == 3)
                    {
                        cout << "You already have a destroyers!" << endl;
                        continue;
                    }
                    type = ShipType::destroyer;
                    break;
                case 3:
                    if(boats == 4)
                    {
                        cout << "You already have a boats!" << endl;
                        continue;
                    }
                    type = ShipType::boat;
                    break;
                default:
                    break;
            }

            if(goodShip(field, x - 1, y - 1, tolower(hor, std::locale()) == 'y', type))
            {
                Ship s(type, x - 1, y - 1, tolower(hor, std::locale()) == 'y');
                switch(shipType)
                {
                    case 0:
                        carriers++;
                        break;
                    case 1:
                        cruisers++;
                        break;
                    case 2:
                        destroyers++;
                        break;
                    case 3:
                        boats++;
                        break;
                    default:
                        break;
                }
                ships.emplace_back(s);
                setShip(field, x - 1, y - 1,tolower(hor, std::locale()) == 'y', type);
                printUsage(field);
            }

            if(Judge::checkShipSet(ships))
                break;
        }
        return ships;
    }
}
