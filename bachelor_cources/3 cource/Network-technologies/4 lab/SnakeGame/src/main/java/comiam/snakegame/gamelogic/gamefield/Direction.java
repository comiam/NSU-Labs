package comiam.snakegame.gamelogic.gamefield;

public enum Direction
{
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static Direction fromNumber(final int num)
    {
        return switch (num)
                {
                    case 1 -> UP;
                    case 2 -> DOWN;
                    case 3 -> LEFT;
                    case 4 -> RIGHT;
                    default -> throw new IllegalArgumentException("Invalid direction number");
                };
    }

    public int toNumber()
    {
        return switch (this)
                {
                    case UP -> 1;
                    case DOWN -> 2;
                    case LEFT -> 3;
                    case RIGHT -> 4;
                };
    }

    public boolean isNotOppositeTo(final Direction other)
    {
        return !switch (this)
                {
                    case UP -> other == DOWN;
                    case DOWN -> other == UP;
                    case LEFT -> other == RIGHT;
                    case RIGHT -> other == LEFT;
                };
    }

    public Direction getOpposite()
    {
        return switch (this)
                {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case LEFT -> RIGHT;
                    case RIGHT -> LEFT;
                };
    }
}
