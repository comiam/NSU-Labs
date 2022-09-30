package comiam.snakegame.gamelogic.snake;

import comiam.snakegame.gamelogic.gamefield.BoundedPoint;
import comiam.snakegame.gamelogic.gamefield.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface SnakeInfo
{

    @NotNull BoundedPoint getHead();

    void forEachSegment(final @NotNull Consumer<@NotNull BoundedPoint> action);

    int getPlayerId();

    @NotNull Direction getDirection();

    boolean isZombie();
}
