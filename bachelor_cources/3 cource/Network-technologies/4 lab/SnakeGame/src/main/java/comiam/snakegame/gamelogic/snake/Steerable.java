package comiam.snakegame.gamelogic.snake;

import comiam.snakegame.gamelogic.gamefield.Direction;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Steerable
{

    void changeDirection(final @NotNull Direction direction);
}
