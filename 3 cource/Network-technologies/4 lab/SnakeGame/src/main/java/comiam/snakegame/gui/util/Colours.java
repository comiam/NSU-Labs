package comiam.snakegame.gui.util;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public final class Colours
{
    public static final Color BACKGROUND_COLOUR = new Color(43, 43, 43); // Dark gray
    public static final Color FOREGROUND_COLOUR = new Color(212, 212, 212); // Almost white

    public static final Color NAME_COLOUR = FOREGROUND_COLOUR;
    public static final Color DEAD_SNAKE_COLOUR = FOREGROUND_COLOUR;
    public static final Color RED = new Color(199, 84, 80); // Red
    public static final Color GREEN = new Color(73, 156, 84); // Green
    public static final Color LIGHT_GRAY = new Color(135, 147, 154); // Light gary
    public static final Color BLUE = new Color(53, 146, 196);
    public static final Color YELLOW = new Color(244, 175, 61);

    public static final Color INTERFACE_BACKGROUND = new Color(60, 63, 65);
    public static final Color GAME_PANEL_BACKGROUND = new Color(43, 43, 43); // Dark gray
    public static final Color LIGHT_LINING = new Color(100, 100, 100);
    public static final Color LINING = new Color(81, 81, 81);
    public static final Color DARK_LINING = new Color(49, 51, 53);
    public static final Color TEXT = new Color(212, 212, 212); // Almost white
    public static final Color SCROLL_THUMB = new Color(78, 78, 78);
    public static final Color TEXT_ENTRY_FORM = new Color(69, 73, 74);
    public static final Color TOOLTIP = new Color(75, 77, 75);

    private static final Collection<Color> snakeColours = new HashSet<>();

    static
    {
        snakeColours.add(LIGHT_GRAY);
        snakeColours.add(BLUE);
        snakeColours.add(YELLOW);
        snakeColours.add(Color.CYAN);
        snakeColours.add(Color.MAGENTA);
    }

    private Colours()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Color getRandomColour()
    {
        var num = ThreadLocalRandom.current().nextInt(0, snakeColours.size());
        for (final var t : snakeColours)
        {
            --num;
            if (num < 0)
            {
                return t;
            }
        }
        throw new AssertionError();
    }
}
