package comiam.snakegame.gamelogic.snake.impl;

import comiam.snakegame.gamelogic.gameobjects.snake.SnakeState;
import comiam.snakegame.gamelogic.gamefield.BoundedMovablePoint;
import comiam.snakegame.gamelogic.gamefield.BoundedPoint;
import comiam.snakegame.gamelogic.gamefield.Coordinates;
import comiam.snakegame.gamelogic.gamefield.Direction;
import comiam.snakegame.gamelogic.snake.SnakeInfo;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class RemoteSnake implements SnakeInfo
{

    private final @NotNull Coordinates bounds;
    private final @NotNull Deque<@NotNull Coordinates> keyPoints = new ArrayDeque<>();

    private @Nullable Direction direction;
    private boolean alive = true;
    private final int playerId;

    public RemoteSnake(
            final int playerId,
            final @NotNull Coordinates bounds,
            final @NotNull SnakeState descriptor)
    {
        this.playerId = playerId;
        this.bounds = bounds;
        this.setKeyPoints(descriptor.getPoints());
        this.setAlive(descriptor.isAlive());
        this.setDirection(descriptor.getDirection());
    }

    private void setKeyPoints(final @NotNull List<@NotNull Coordinates> keyPoints)
    {
        this.keyPoints.clear();
        this.keyPoints.addAll(keyPoints);
    }

    private void setAlive(final boolean alive)
    {
        this.alive = alive;
    }

    private void setDirection(final @NotNull Direction direction)
    {
        this.direction = direction;
    }

    @Override
    public @NotNull BoundedPoint getHead()
    {
        if (this.keyPoints.isEmpty())
        {
            throw new IllegalStateException("Headless");
        }
        return new BoundedMovablePoint(this.keyPoints.peekFirst(), this.bounds);
    }

    @Override
    public @NotNull Direction getDirection()
    {
        if (this.direction == null)
        {
            throw new IllegalStateException("Lost it's way"); // i'm probably gonna hate myself for these error messages
        }
        return this.direction;
    }

    @Override
    public void forEachSegment(
            final @NotNull Consumer<@NotNull BoundedPoint> action)
    {
        if (this.keyPoints.isEmpty())
        {
            return;
        }

        val head = this.keyPoints.peekFirst();
        val point = new BoundedMovablePoint(head, this.bounds);
        action.accept(point);
        this.keyPoints.stream()
                .skip(1)
                .forEach(offset -> {
                    point.forEachWithinExceptSelf(offset, action);
                    point.move(offset);
                });
    }

    @Override
    public int getPlayerId()
    {
        return this.playerId;
    }

    @Override
    public boolean isZombie()
    {
        return !this.alive;
    }
}
