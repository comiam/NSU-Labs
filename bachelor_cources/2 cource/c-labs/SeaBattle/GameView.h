#ifndef SEABATTLE_GAMEVIEW_H
#define SEABATTLE_GAMEVIEW_H
#include <vector>
#include <string>
#include "Player.h"
#include <windows.h>

using namespace std;

void showGame(vector<vector<int>> field0, vector<vector<int>> field1, bool hide0, bool hide1, int round, int score0, int score1, bool showStats);
void showFieldEditor(vector<vector<int>> field0);
void showWinner0(bool first);
void showWinner1(bool first);
bool ask(const string& question);
void viewMessage(const string& msg);

void moveCenter();
void createWindow(int width, int height);

#endif