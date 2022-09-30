package comiam.sapper.game;

import comiam.sapper.time.Timer;
import comiam.sapper.ui.GameViewController;
import comiam.sapper.ui.gui.GUIMenu;
import comiam.sapper.ui.tui.TextMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static comiam.sapper.util.BitOperations.getBit;
import static comiam.sapper.util.BitOperations.setBit;
import static comiam.sapper.util.CoordinateUtils.getDotsNear;
import static comiam.sapper.util.CoordinateUtils.isAvailable;
import static comiam.sapper.util.GUIUtils.invokeController;

public class Minesweeper
{
    public static final String GAME_VERSION = "1.31.";
    private static int flagCount;
    private static int maxFlagCount;
    private static boolean gameStarted = false;
    private static boolean gameEnded = false;
    private static byte[][] map;
    private static int row, col;
    private static final ArrayList<GameViewController> interfaces = new ArrayList<>();
    private static GameViewController mainController;

    public static void showMenu(boolean two, boolean mainGUI)
    {
        if(mainGUI)
            invokeController(null, () -> new GUIMenu(two));
        else
            new TextMenu(two);
    }

    public static void newGame()
    {
        flagCount = 0;
        gameStarted = false;
        gameEnded = false;
        map = new byte[col][row];

        Timer.clearTime();
    }

    public static void newGame(FieldDimension dim)
    {
        flagCount = 0;
        maxFlagCount = 0;
        switch(dim)
        {
            case x16:
                row = 16;
                col = 16;
                maxFlagCount = 50;
                break;
            case x32:
                row = 32;
                col = 32;
                maxFlagCount = 100;
                break;
            case nothing:
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + dim);
        }

        newGame();
    }

    public static void initField(int _x, int _y)
    {
        int mines = maxFlagCount;
        int x, y;
        while(mines > 0)
        {
            x = ThreadLocalRandom.current().nextInt(col);
            y = ThreadLocalRandom.current().nextInt(row);

            if(Math.abs(x - _x) <= 1 && Math.abs(y - _y) <= 1)
                continue;

            if(!isMined(map[x][y]))//TODO 000 - flags of cell, where first bit - mine flag, second - mark flag, third - maybe mark flag
            {
                map[x][y] = setBit(map[x][y], 1, 0);//TODO mine cell
                mines--;
            }
        }
    }

    public static void openCell(int x, int y)
    {
        openCell(x,y, true);
    }

    public static void openCell(int x, int y, boolean main)
    {
        if(!isNotTagged(x,y))
        {
            mainController.update(true);
            return;
        }

        if(isMarked(map[x][y]) || isMaybeMarked(map[x][y]))
        {
            if(isMarked(map[x][y]))
                flagCount--;
            removeMark(x, y);
            removeMaybeMark(x, y);

            for(var in : interfaces)
                invokeController(in, () -> {
                    in.repaintFlag();
                    in.markCell(x,y);
                });
        }

        if(isMined(map[x][y]))
            gameOver();
        else
        {
            tagCell(x, y);
            if(minedNearCell(x, y) > 0)
                for(var in : interfaces)
                    invokeController(in, () -> in.setNumCell(x, y, minedNearCell(x, y)));
            else
            {
                for(var in : interfaces)
                    invokeController(in, () -> in.freeCell(x, y));

                for(var point : getDotsNear(x,y))
                    if(isAvailable(point.x, point.y) && minedNearCell(point.x, point.y) == 0 && isNotTagged(point.x, point.y))
                        openCell(point.x, point.y, false);
                    else if(isAvailable(point.x, point.y) && minedNearCell(point.x, point.y) != 0)
                    {
                        tagCell(point.x, point.y);
                        if(isMarked(map[point.x][point.y]) || isMaybeMarked(map[point.x][point.y]))
                        {
                            for(var in : interfaces)
                                invokeController(in, () -> in.offMarkOnCell(point.x, point.y));

                            if(isMarked(map[point.x][point.y]))
                            {
                                flagCount--;
                                for(var in : interfaces)
                                    invokeController(in, in::repaintFlag);
                            }

                            removeMaybeMark(point.x, point.y);
                            removeMark(point.x,point.y);
                        }
                        for(var in : interfaces)
                            invokeController(in, () -> in.setNumCell(point.x, point.y, minedNearCell(point.x, point.y)));
                    }
            }
        }
        if(checkForWin())
            gameWin();

        if(main && !gameEnded && !checkForWin() && gameStarted)
            for(var in : interfaces)
                in.update(false);
    }

    public static void markCell(int x, int y)
    {
        if(!isNotTagged(x, y))
        {
            mainController.update(true);
            return;
        }

        if(isMarked(map[x][y]))
        {
            removeMark(x,y);
            maybeMark(x,y);

            flagCount--;
            for(var in : interfaces)
                invokeController(in,() -> {
                    in.repaintFlag();
                    in.markMaybeCell(x, y);
                });
        } else if(isMaybeMarked(map[x][y]))
        {
            removeMaybeMark(x, y);
            for(var in : interfaces)
                invokeController(in, () -> in.offMarkOnCell(x, y));
        } else
        {
            if(flagCount >= maxFlagCount)
                return;
            mark(x, y);

            flagCount++;
            for(var in : interfaces)
                invokeController(in, () -> {
                    in.repaintFlag();
                    in.markCell(x, y);
                });
        }
        if(checkForWin())
            gameWin();

        for(var in : interfaces)
            in.update(false);
    }

    private static boolean checkForWin()
    {
        for(byte[] bytes : map)
            for(byte aByte : bytes)
                if((isMined(aByte) && !isMarked(aByte)) || (!isMined(aByte) && isNotTagged(aByte)))
                    return false;
        return true;
    }

    private static int minedNearCell(int x, int y)
    {
        int count = 0;

        for(var p : getDotsNear(x,y))
            if(isAvailable(p.x, p.y) && isMined(map[p.x][p.y]))
                count++;

        return count;
    }

    public static boolean isMined(byte m)
    {
        return getBit(m, 0) == 1;
    }

    public static boolean isMarked(byte m)
    {
        return getBit(m, 1) == 1;
    }

    public static boolean isMaybeMarked(byte m)
    {
        return getBit(m, 2) == 1;
    }

    private static void maybeMark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 1, 2);
    }

    private static void mark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 1, 1);
    }

    private static void removeMark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 0, 1);
    }

    private static void removeMaybeMark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 0, 2);
    }

    private static void tagCell(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 1, 3);
    }

    private static boolean isNotTagged(int x, int y)
    {
        return isNotTagged(map[x][y]);
    }

    private static boolean isNotTagged(byte aByte)
    {
        return getBit(aByte, 3) != 1;
    }

    private static void gameOver()
    {
        gameEnded = true;
        gameStarted = false;

        for(var in : interfaces)
            invokeController(in, () -> in.disableGame(map));

        for(var in : interfaces)
            invokeController(in, in::noticeOverGame);
    }

    private static void gameWin()
    {
        gameEnded = true;
        gameStarted = false;
        for(var in : interfaces)
            invokeController(in, () -> in.disableGame(map));

        for(var in : interfaces)
            invokeController(in, in::noticeWinGame);
    }

    public static int getFlagCount()
    {
        return flagCount;
    }

    public static int getMaxFlagCount()
    {
        return maxFlagCount;
    }

    public static boolean isGameEnded()
    {
        return gameEnded;
    }

    public static boolean isGameStarted()
    {
        return gameStarted;
    }

    public static void startGame()
    {
        gameStarted = true;
    }

    public static Dimension getFieldSize()
    {
        return new Dimension(col, row);
    }

    public static void removeController(GameViewController controller)
    {
        interfaces.remove(controller);
    }

    public static void addController(GameViewController controller)
    {
        if(!interfaces.contains(controller))
            interfaces.add(controller);
    }

    public static void setMainController(GameViewController controller)
    {
        if(mainController != null)
            return;

        addController(controller);
        mainController = controller;
    }

    public static boolean isMainControllerIsGUI()
    {
        return mainController != null && mainController.isGUI();
    }

    public static boolean isMainController(GameViewController controller)
    {
        return mainController.hashCode() == controller.hashCode();
    }

    public static void repaintControllersTimer()
    {
        for(var in : interfaces)
            invokeController(in,in::repaintTimer);
    }

    public static void rebuildControllers()
    {
        AtomicBoolean access = new AtomicBoolean(false);
        invokeController(mainController, () -> access.set(mainController.rebuildField()));

        if(access.get())
            for(var in : interfaces)
                if(!isMainController(in))
                    invokeController(in, in::rebuildField);
    }

    public static void restartControllers()
    {
        AtomicBoolean access = new AtomicBoolean(false);
        invokeController(mainController, () -> access.set(mainController.restartGame()));
        
        if(access.get())
            for(var in : interfaces)
                if(!isMainController(in))
                    invokeController(in, in::restartGame);
    }

    public static void pauseControllers()
    {
        for(var in : interfaces)
            if(!isMainController(in))
                invokeController(in, in::setPause);
    }

    public enum FieldDimension
    {
        x16,
        x32,
        nothing
    }
}
