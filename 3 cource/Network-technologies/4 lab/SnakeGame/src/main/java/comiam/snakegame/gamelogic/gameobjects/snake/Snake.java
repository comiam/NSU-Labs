package comiam.snakegame.gamelogic.gameobjects.snake;

import comiam.snakegame.gamelogic.gamefield.Coordinates;
import comiam.snakegame.gamelogic.gamefield.Direction;

import java.util.List;

public class Snake implements SnakeState
{
    private final int playerId;
    private final List<Coordinates> points;
    private final boolean alive;
    private final Direction direction;

    public Snake(int playerId, List<Coordinates> points, boolean alive, Direction direction)
    {
        this.playerId = playerId;
        this.points = points;
        this.alive = alive;
        this.direction = direction;
    }

    public int getPlayerId()
    {
        return this.playerId;
    }

    public List<Coordinates> getPoints()
    {
        return this.points;
    }

    public boolean isAlive()
    {
        return this.alive;
    }

    public Direction getDirection()
    {
        return this.direction;
    }

    public boolean equals(final Object o)
    {
        if (o == this) return true;
        if (!(o instanceof Snake)) return false;
        final Snake other = (Snake) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPlayerId() != other.getPlayerId()) return false;
        return true;
    }

    protected boolean canEqual(final Object other)
    {
        return other instanceof Snake;
    }

    public int hashCode()
    {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPlayerId();
        return result;
    }
}
