#include "GameView.h"

inline void clear()
{
    system("cls");
}

void createWindow(int width, int height)
{
    system("color F0");
    HWND hwnd;
    char Title[1024];
    GetConsoleTitle(Title, 1024);
    hwnd = FindWindow(nullptr, Title);
    MoveWindow(hwnd, 0, 0, width, height, TRUE);
}

void moveCenter()
{
    RECT rectClient, rectWindow;
    HWND hWnd = GetConsoleWindow();
    GetClientRect(hWnd, &rectClient);
    GetWindowRect(hWnd, &rectWindow);
    int posx, posy;
    posx = GetSystemMetrics(SM_CXSCREEN) / 2 - (rectWindow.right - rectWindow.left) / 2,
            posy = GetSystemMetrics(SM_CYSCREEN) / 2 - (rectWindow.bottom - rectWindow.top) / 2,
            MoveWindow(hWnd, posx, posy, rectClient.right - rectClient.left, rectClient.bottom - rectClient.top, TRUE);
}

void showGame(vector<vector<int>> field0, vector<vector<int>> field1, bool hide0, bool hide1, int round, int score0, int score1, bool showStats)
{
    clear();
    cout << "  1 2 3 4 5 6 7 8 9 10   1 2 3 4 5 6 7 8 9 10" << endl;
    for(int i = 0; i < 10; ++i)
    {
        cout << (i + 1 == 10 ? to_string(i + 1) : to_string(i + 1) + " ");
        for(int j = 0; j < 10; ++j)
        {
            switch(field0[i][j])
            {
                case PLAYER_SHIP:
                    if(!hide0)
                    {
                        cout << "#";
                        break;
                    }
                case EMPTY_FIELD:
                    cout << "~";
                    break;
                case DAMAGED:
                    cout << "X";
                    break;
                case FIRED:
                    cout << "*";
                    break;
            }
            cout << " ";
        }
        cout << (i + 1 == 10 ? " " + to_string(i + 1) : " " + to_string(i + 1) + " ");
        for(int j = 0; j < 10; ++j)
        {
            switch(field1[i][j])
            {
                case PLAYER_SHIP:
                    if(!hide1)
                    {
                        cout << "#";
                        break;
                    }
                case EMPTY_FIELD:
                    cout << "~";
                    break;
                case DAMAGED:
                    cout << "X";
                    break;
                case FIRED:
                    cout << "*";
                    break;
            }
            cout << " ";
        }

        if(i == 4 && showStats)
            cout << "       round: " + to_string(round);
        if(i == 5 && showStats)
            cout << "       score first player: " + to_string(score0);
        if(i == 6 && showStats)
            cout << "       score second player: " + to_string(score1);

        cout << endl;
    }
}

void showFieldEditor(vector<vector<int>> field0)
{
    clear();
    cout << "  1 2 3 4 5 6 7 8 9 10" << endl;
    for(int i = 0; i < 10; ++i)
    {
        cout << (i + 1 == 10 ? to_string(i + 1) : to_string(i + 1) + " ");
        for(int j = 0; j < 10; ++j)
        {
            switch(field0[i][j])
            {
                case PLAYER_SHIP:
                    cout << "#";
                    break;
                case EMPTY_FIELD:
                    cout << "~";
                    break;
                case DAMAGED:
                    cout << "X";
                    break;
                case FIRED:
                    cout << "*";
                    break;
            }
            cout << " ";
        }
        cout << endl;
    }
}

void showWinner0(bool first)
{
    clear();
    if(first)
        cout << R"(
 __    _                  _
/  |  (_)                (_)
`| |   _  ___  __      __ _  _ __   _ __    ___  _ __
 | |  | |/ __| \ \ /\ / /| || '_ \ | '_ \  / _ \| '__|
_| |_ | |\__ \  \ V  V / | || | | || | | ||  __/| |
\___/ |_||___/   \_/\_/  |_||_| |_||_| |_| \___||_|


)" << endl;
    else
        cout << R"(
  ___    _                  _
 |__ \  (_)                (_)
    ) |  _  ___  __      __ _  _ __   _ __    ___  _ __
   / /  | |/ __| \ \ /\ / /| || '_ \ | '_ \  / _ \| '__|
  / /_  | |\__ \  \ V  V / | || | | || | | ||  __/| |
 |____| |_||___/   \_/\_/  |_||_| |_||_| |_| \___||_|


)" << endl;
}

void showWinner1(bool first)
{
    clear();
    if(first)
        cout << R"(
  __   _         __  _                _             _
 /_ | (_)       / _|(_)              | |           (_)
  | |  _  ___  | |_  _  _ __    __ _ | | __      __ _  _ __   _ __    ___  _ __
  | | | |/ __| |  _|| || '_ \  / _` || | \ \ /\ / /| || '_ \ | '_ \  / _ \| '__|
  | | | |\__ \ | |  | || | | || (_| || |  \ V  V / | || | | || | | ||  __/| |
  |_| |_||___/ |_|  |_||_| |_| \__,_||_|   \_/\_/  |_||_| |_||_| |_| \___||_|


)" << endl;
    else
        cout << R"(
  ___    _         __  _                _             _
 |__ \  (_)       / _|(_)              | |           (_)
    ) |  _  ___  | |_  _  _ __    __ _ | | __      __ _  _ __   _ __    ___  _ __
   / /  | |/ __| |  _|| || '_ \  / _` || | \ \ /\ / /| || '_ \ | '_ \  / _ \| '__|
  / /_  | |\__ \ | |  | || | | || (_| || |  \ V  V / | || | | || | | ||  __/| |
 |____| |_||___/ |_|  |_||_| |_| \__,_||_|   \_/\_/  |_||_| |_||_| |_| \___||_|


)" << endl;
}

bool ask(const string& question)
{
    clear();
    cout << question << endl;
    char a;
    while(true)
    {
        cin >> a;
        if(!cin)
        {
            cout << question << endl;
            continue;
        }
        if(!(tolower(a, std::locale()) == 'y' || tolower(a, std::locale()) == 'n'))
            continue;
        else
            return tolower(a, std::locale()) == 'y';
    }
}

void viewMessage(const string& msg)
{
    clear();
    cout << msg << endl;
}//-fRandom -sRandom -c 1