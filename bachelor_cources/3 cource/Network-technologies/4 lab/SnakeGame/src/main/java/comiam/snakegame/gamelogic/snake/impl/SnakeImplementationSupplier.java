package comiam.snakegame.gamelogic.snake.impl;

import comiam.snakegame.gamelogic.gamefield.BoundedPoint;
import comiam.snakegame.gamelogic.gamefield.Direction;
import org.jetbrains.annotations.NotNull;
import comiam.snakegame.gamelogic.snake.Snake;

@FunctionalInterface
public interface SnakeImplementationSupplier<SnakeType extends Snake>
{

    @NotNull SnakeType get(
            final int id,
            final @NotNull BoundedPoint head,
            final @NotNull Direction direction,
            final int size);
}
