package comiam.snakegame.gamelogic.gameobjects;

import comiam.snakegame.gamelogic.gamefield.Coordinates;
import me.ippolitov.fit.snakes.SnakesProto;

public final class Utility
{
    private Utility()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static SnakesProto.GameState.Coord coordinates(final Coordinates coordinates)
    {
        return SnakesProto.GameState.Coord.newBuilder()
                .setX(coordinates.getX())
                .setY(coordinates.getY())
                .build();
    }
}
