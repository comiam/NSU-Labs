package comiam.snakegame.gamelogic.snake.impl;

import comiam.snakegame.gamelogic.gamefield.BoundedMovablePoint;
import comiam.snakegame.gamelogic.gamefield.BoundedPoint;
import comiam.snakegame.gamelogic.gamefield.Direction;
import comiam.snakegame.gamelogic.snake.Snake;
import comiam.snakegame.gamelogic.snake.SnakeInfo;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Function;

public class LocalSnake implements Snake
{

    private final @NotNull Deque<BoundedMovablePoint> segments = new ArrayDeque<>();
    private @NotNull Direction direction;
    private @NotNull Direction nextDirection;
    private boolean alive = true;
    private final int playerId;

    public LocalSnake(
            final int playerId,
            final @NotNull BoundedPoint headPosition,
            final @NotNull Direction direction,
            final int size)
    {
        if (size <= 0)
        {
            throw new IllegalArgumentException("Invalid size");
        }

        this.playerId = playerId;

        val head = new BoundedMovablePoint(headPosition);
        this.segments.add(head);
        for (int i = 1; i < size; i += 1)
        {
            this.segments.add(this.segments.getLast().moved(direction.getOpposite()));
        }

        this.direction = direction;
        this.nextDirection = direction;
    }

    public static @NotNull LocalSnake copyOf(final @NotNull SnakeInfo other)
    {
        return new LocalSnake(other);
    }

    protected LocalSnake(final @NotNull SnakeInfo other)
    {
        this.playerId = other.getPlayerId();
        other.forEachSegment(point -> this.segments.add(new BoundedMovablePoint(point)));
        if (other.isZombie())
        {
            this.zombify();
        }
        this.direction = other.getDirection();
        this.nextDirection = this.direction;
    }

    protected boolean isOwnSegment(final @NotNull BoundedMovablePoint point)
    {
        return this.segments.contains(point);
    }

    @Override
    public void move(final @NotNull Function<@NotNull BoundedPoint, @NotNull Boolean> isFood)
    {
        val head = this.segments.peekFirst();
        if (head == null)
        {
            throw new IllegalStateException("Headless");
        }
        this.segments.push(head.moved(this.nextDirection));
        if (!isFood.apply(this.getHead()))
        {
            this.segments.removeLast();
        }
        this.direction = this.nextDirection;
    }

    @Override
    public void zombify()
    {
        this.alive = false;
    }

    @Override
    public void changeDirection(
            final @NotNull Direction direction)
    {
        if (this.direction.isNotOppositeTo(direction))
        {
            this.nextDirection = direction;
        }
    }

    @Override
    public @NotNull BoundedPoint getHead()
    {
        val head = this.segments.peekFirst();
        if (head == null)
        {
            throw new IllegalStateException("Headless");
        }
        return head;
    }

    @Override
    public void forEachSegment(
            final @NotNull Consumer<@NotNull BoundedPoint> action)
    {
        this.segments.forEach(action);
    }

    @Override
    public int getPlayerId()
    {
        return this.playerId;
    }

    @Override
    public @NotNull Direction getDirection()
    {
        return this.direction;
    }

    @Override
    public boolean isZombie()
    {
        return !this.alive;
    }
}
