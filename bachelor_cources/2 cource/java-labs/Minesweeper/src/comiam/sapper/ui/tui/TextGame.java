package comiam.sapper.ui.tui;

import comiam.sapper.game.Minesweeper;
import comiam.sapper.game.records.Pair;
import comiam.sapper.game.records.ScoreRecords;
import comiam.sapper.time.Timer;
import comiam.sapper.ui.GameViewController;
import comiam.sapper.util.CoordinateUtils;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static comiam.sapper.time.Timer.*;
import static comiam.sapper.util.IOUtils.print;
import static comiam.sapper.util.IOUtils.println;
import static comiam.sapper.util.TextUtils.*;
import static java.lang.Integer.parseInt;

public class TextGame implements GameViewController
{
    private static final int FREE_CELL = 0;
    private static final int NOT_OPENED_CELL = -1;
    private static final int MARKED_CELL = -2;
    private static final int MARKED_MAYBE_CELL = -3;
    private static final int MINED_CELL = -4;
    private Console console;
    private Scanner scanner;

    private int[][] map;
    private boolean gameOverFlag = false;
    private boolean gameWinFlag = false;
    private boolean stopFlag = false;
    private boolean useConsole = true;

    public void init(boolean main)
    {
        map = new int[Minesweeper.getFieldSize().width][Minesweeper.getFieldSize().height];

        for(var arr : map)
            Arrays.fill(arr, NOT_OPENED_CELL);

        display(true);
        if(!main)
            return;

        if((console = System.console()) == null)
        {
            useConsole = false;
            scanner = new Scanner(System.in);
        }

        String line;
        do
        {
            if(useConsole)
                line = console.readLine().trim();
            else
                line = scanner.nextLine().trim();

            if(line.split(" ").length > 3)
            {
                println("Unknown command: " + line);
                if(Minesweeper.isMainController(this))
                    print("> ");
                continue;
            }

            String[] lineArr = line.split(" ");

            if(lineArr.length == 3 && isNumeric(lineArr[1]) && isNumeric(lineArr[2]) && !isNumeric(lineArr[0]))
            {
                if(!CoordinateUtils.isAvailable(parseInt(lineArr[1]), parseInt(lineArr[2])))
                {
                    println("Wrong coordinates!");
                    if(Minesweeper.isMainController(this))
                        print("> ");
                    continue;
                }
                if(lineArr[0].length() != 1 || !"mo".contains(lineArr[0]))
                {
                    println("Wrong mode!");
                    if(Minesweeper.isMainController(this))
                        print("> ");
                    continue;
                }

                if(!Minesweeper.isGameStarted())
                {
                    Minesweeper.initField(parseInt(lineArr[1]), parseInt(lineArr[2]));
                    Minesweeper.startGame();
                    start(Minesweeper::repaintControllersTimer);
                }
                if(Minesweeper.isGameEnded())
                {
                    if(Minesweeper.isMainController(this))
                        print("> lol");
                    continue;
                }
                switch(lineArr[0])
                {
                    case "o" -> Minesweeper.openCell(parseInt(lineArr[1]), parseInt(lineArr[2]));
                    case "m" -> Minesweeper.markCell(parseInt(lineArr[1]), parseInt(lineArr[2]));
                }
                continue;
            }

            switch(line)
            {
                case "help", "?" -> {
                    println("""
                               o <x> <y> - open cell with this coordinates
                               m <x> <y> - mark/mark for sure/unmark cell with this coordinates
                               pause     - pause a timer of game
                               replay    - replay game with same field size
                               new       - play a new game
                               exit      - exit from game
                            """);
                    print("> ");
                }
                case "pause" -> {
                    if(!Minesweeper.isGameStarted())
                    {
                        if(Minesweeper.isMainController(this))
                            print("> ");
                        continue;
                    }
                    Minesweeper.pauseControllers();
                    setPause();
                }
                case "replay" -> {
                    if(!Minesweeper.isGameEnded() && !Minesweeper.isGameStarted())
                    {
                        if(Minesweeper.isMainController(this))
                            print("> ");
                        continue;
                    }
                    Minesweeper.restartControllers();
                }
                case "new" -> Minesweeper.rebuildControllers();
                case "exit" -> System.exit(0);
                default -> {
                    if(gameWinFlag || gameOverFlag || line.isEmpty())
                    {
                        if(Minesweeper.isMainController(this))
                            print("> ");
                        continue;
                    }
                    println("Unknown command: " + line);
                    if(Minesweeper.isMainController(this))
                        print("> ");
                }
            }
        } while(true);
    }

    private void display(boolean getInvite)
    {
        println(getTimeString(getSeconds() / 3600) + ":" + getTimeString((getSeconds() % 3600) / 60) + ":" + getTimeString(getSeconds() % 60));
        println(Minesweeper.getFlagCount() + "/" + Minesweeper.getMaxFlagCount());

        if(stopFlag)
        {
            println("""
                    (  _ \\ / _\\ / )( \\/ ___)(  __)
                     ) __//    \\) \\/ (\\___ \\ ) _)
                    (__)  \\_/\\_/\\____/(____/(____)
                    """);
            if(Minesweeper.isMainController(this))
                print("> ");
            return;
        }

        print("  ");
        for(int i = 0; i < map.length; i++)
            print(getNumericString(i));
        println();

        for(int x = 0; x < map.length; x++)
        {
            print(getNumericString(x));
            for(int y = 0; y < map[x].length; y++)
                switch(map[y][x])
                {
                    case MINED_CELL -> print(" *");
                    case NOT_OPENED_CELL -> print(" ?");
                    case MARKED_CELL -> print(" F");
                    case MARKED_MAYBE_CELL -> print(" V");
                    case FREE_CELL -> print("  ");
                    case 1, 2, 3, 4, 5, 6, 7, 8 -> print(" " + map[y][x]);
                    default -> throw new IllegalArgumentException("Wrong map encoding: " + map[y][x]);
                }
            println();
        }
        if(gameOverFlag)
            println("""
                        ▓██   ██▓ ▒█████   █    ██     ██▓     ▒█████    ██████ ▓█████
                         ▒██  ██▒▒██▒  ██▒ ██  ▓██▒   ▓██▒    ▒██▒  ██▒▒██    ▒ ▓█   ▀
                          ▒██ ██░▒██░  ██▒▓██  ▒██░   ▒██░    ▒██░  ██▒░ ▓██▄   ▒███  
                          ░ ▐██▓░▒██   ██░▓▓█  ░██░   ▒██░    ▒██   ██░  ▒   ██▒▒▓█  ▄
                          ░ ██▒▓░░ ████▓▒░▒▒█████▓    ░██████▒░ ████▓▒░▒██████▒▒░▒████▒
                           ██▒▒▒ ░ ▒░▒░▒░ ░▒▓▒ ▒ ▒    ░ ▒░▓  ░░ ▒░▒░▒░ ▒ ▒▓▒ ▒ ░░░ ▒░ ░
                         ▓██ ░▒░   ░ ▒ ▒░ ░░▒░ ░ ░    ░ ░ ▒  ░  ░ ▒ ▒░ ░ ░▒  ░ ░ ░ ░  ░
                         ▒ ▒ ░░  ░ ░ ░ ▒   ░░░ ░ ░      ░ ░   ░ ░ ░ ▒  ░  ░  ░     ░  
                         ░ ░         ░ ░     ░            ░  ░    ░ ░        ░     ░  ░
                         ░ ░                                                          
                        """);
        else if(gameWinFlag)
            println("""
                    ██╗   ██╗ ██████╗ ██╗   ██╗    ██╗    ██╗██╗███╗   ██╗
                    ╚██╗ ██╔╝██╔═══██╗██║   ██║    ██║    ██║██║████╗  ██║
                     ╚████╔╝ ██║   ██║██║   ██║    ██║ █╗ ██║██║██╔██╗ ██║
                      ╚██╔╝  ██║   ██║██║   ██║    ██║███╗██║██║██║╚██╗██║
                       ██║   ╚██████╔╝╚██████╔╝    ╚███╔███╔╝██║██║ ╚████║
                       ╚═╝    ╚═════╝  ╚═════╝      ╚══╝╚══╝ ╚═╝╚═╝  ╚═══╝
                    """);

        if(Minesweeper.isMainController(this) && getInvite)
            print("> ");
    }

    @Override
    public void markCell(int x, int y)
    {
        map[x][y] = MARKED_CELL;
        display(true);
    }

    @Override
    public void markMaybeCell(int x, int y)
    {
        map[x][y] = MARKED_MAYBE_CELL;
        display(true);
    }

    @Override
    public void offMarkOnCell(int x, int y)
    {
        map[x][y] = NOT_OPENED_CELL;
    }

    @Override
    public void freeCell(int x, int y)
    {
        map[x][y] = FREE_CELL;
    }

    @Override
    public void setPause()
    {
        if(Minesweeper.isMainController(this))
        {
            if(isRunning())
                stop();
            else
                on();
        }
        stopFlag = !stopFlag;

        display(true);
    }

    @Override
    public void setNumCell(int x, int y, int num)
    {
        map[x][y] = (byte) num;
    }

    @Override
    public void repaintTimer() {}

    @Override
    public void repaintFlag() {}

    @Override
    public void disableGame(byte[][] map)
    {
        if(Minesweeper.isMainController(this))
            stop();
        for(int x = 0; x < map.length; x++)
            for(int y = 0; y < map[x].length; y++)
                if(Minesweeper.isMined(map[x][y]))
                    this.map[x][y] = MINED_CELL;
    }

    @Override
    public boolean restartGame()
    {
        gameWinFlag = false;
        gameOverFlag = false;
        stopFlag = false;
        for(var arr : map)
            Arrays.fill(arr, (byte) NOT_OPENED_CELL);
        if(Minesweeper.isMainController(this))
        {
            stop();
            Minesweeper.newGame();
        }
        display(true);
        return true;
    }

    @Override
    public boolean rebuildField()
    {
        if(Minesweeper.isMainController(this))
        {
            Timer.stop();
            println("Pls, select field size: x16 or x32");
            String line;
            Minesweeper.FieldDimension dim;

            menu:
            do
            {
                if(Minesweeper.isMainController(this))
                    print("> ");

                if(useConsole)
                    line = console.readLine().trim();
                else
                    line = scanner.nextLine().trim();

                if(line.split(" ").length > 1)
                {
                    println("Unknown size: " + line);
                    continue;
                }

                switch(line)
                {
                    case "x16" -> {
                        dim = Minesweeper.FieldDimension.x16;
                        break menu;
                    }
                    case "x32" -> {
                        dim = Minesweeper.FieldDimension.x32;
                        break menu;
                    }
                    case "nothing" -> {
                        start(Minesweeper::repaintControllersTimer);
                        return false;
                    }
                    case "exit" -> System.exit(0);
                    default -> println("Unknown size: " + line);
                }
            } while(true);

            Minesweeper.newGame(dim);
        }
        map = new int[Minesweeper.getFieldSize().width][Minesweeper.getFieldSize().height];
        for(var arr : map)
            Arrays.fill(arr, NOT_OPENED_CELL);

        gameOverFlag = false;
        gameWinFlag = false;
        stopFlag = false;

        display(true);

        return true;
    }

    @Override
    public void noticeOverGame()
    {
        gameOverFlag = true;
        gameWinFlag = false;

        display(false);
        println("""
                    Well, now you can enter:
                    replay - replay game with same field size
                    new    - play a new game
                    exit   - exit from game
                    """);
        print("> ");
    }

    @Override
    public void noticeWinGame()
    {
        gameWinFlag = true;
        gameOverFlag = false;

        display(false);

        println("Pls, enter your name:)\nAnd don't use ';'! Maximum lenght - 10 symbols:");
        String name;
        do
        {
            print("> ");
            if(useConsole)
                name = console.readLine().trim();
            else
                name = scanner.nextLine().trim();
        }while(name.length() > 10 && name.contains(";"));

        if(name.isEmpty())
        {
            name = (System.getProperty("user.name") == null ? "user" : System.getProperty("user.name").isEmpty() ? "user" : System.getProperty("user.name"));
            println("Okay... I will write name " + name);
        }

        Pair pair = new Pair(name + ";" + getTimeString(getSeconds() / 3600) + ":" +
                          getTimeString((getSeconds() % 3600) / 60) + ":" +
                          getTimeString(getSeconds() % 60));
        ScoreRecords.saveRecord(pair);

        println("Score list:");
        ArrayList<Pair> pairs = ScoreRecords.getRecords();
        if(pairs == null)
        {
            println("Score list is empty!\n\n");
        }else
            for(int i = 0; i < pairs.size();i++)
                println((i + 1) + ") " + pairs.get(i).getName() + ": " + pairs.get(i).getTime());

        println();
        println();
        println("""
                    Well, now you can enter:
                    replay - replay game with same field size
                    new    - play a new game
                    exit   - exit from game
                    """);
    }

    @Override
    public boolean isGUI()
    {
        return false;
    }

    @Override
    public void update(boolean makeOnlyOutSymbol)
    {
        if(makeOnlyOutSymbol || gameOverFlag || gameWinFlag)
            print("> ");
        else
            display(true);
    }

}
