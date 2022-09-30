package comiam.sapper.util;

import comiam.sapper.game.Minesweeper;

import java.awt.*;

public class CoordinateUtils
{
    public static boolean isAvailable(int x, int y)
    {
        return x < Minesweeper.getFieldSize().width && y < Minesweeper.getFieldSize().height && x >= 0 && y >= 0;
    }

    public static Point[] getDotsNear(int x, int y)
    {
        return new Point[]{
                new Point(x - 1, y),
                new Point(x, y - 1),
                new Point(x + 1, y),
                new Point(x, y + 1),
                new Point(x - 1, y - 1),
                new Point(x + 1, y - 1),
                new Point(x - 1, y + 1),
                new Point(x + 1, y + 1)
        };
    }
}
