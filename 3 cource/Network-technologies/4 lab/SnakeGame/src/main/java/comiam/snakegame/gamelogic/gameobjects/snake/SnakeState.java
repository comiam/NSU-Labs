package comiam.snakegame.gamelogic.gameobjects.snake;

import comiam.snakegame.gamelogic.gameobjects.Utility;
import comiam.snakegame.gamelogic.gamefield.*;
import comiam.snakegame.gamelogic.snake.SnakeInfo;
import me.ippolitov.fit.snakes.SnakesProto;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public interface SnakeState
{

    static SnakeState forSnake(final SnakeInfo snake)
    {
        var playerId = snake.getPlayerId();

        var points = new ArrayList<Coordinates>();
        var lastKeyPoint = new BoundedMovablePoint(snake.getHead(), snake.getHead().getBounds());
        var previous = new BoundedMovablePoint(snake.getHead(), snake.getHead().getBounds());
        var offset = new UnboundedPoint();
        var bounds = snake.getHead().getBounds();

        // i'm not proud of this if you've been wondering
        snake.forEachSegment(it -> {
            if (it == snake.getHead())
            {
                offset.x = lastKeyPoint.getX();
                offset.y = lastKeyPoint.getY();
                points.add(offset.toCoordinates());
                offset.x = 0;
                offset.y = 0;
            } else
            {
                if (lastKeyPoint.getX() != it.getX() && lastKeyPoint.getY() != it.getY())
                {
                    lastKeyPoint.setCoordinates(previous);
                    points.add(offset.toCoordinates());
                    offset.x = 0;
                    offset.y = 0;
                }
                var dx = it.getX() - previous.getX();
                var dy = it.getY() - previous.getY();
                if (abs(dx) == 1 && abs(dy) == 0 || abs(dx) == 0 && abs(dy) == 1)
                { // no warp
                    offset.x += dx;
                    offset.y += dy;
                } else if (abs(dx) == bounds.getX() - 1 && abs(dy) == 0)
                { // x warp
                    if (dx > 0)
                    {
                        offset.x -= 1;
                    } else
                    {
                        offset.x += 1;
                    }
                } else if (abs(dx) == 0 && abs(dy) == bounds.getY() - 1)
                { // y warp
                    if (dy > 0)
                    {
                        offset.y -= 1;
                    } else
                    {
                        offset.y += 1;
                    }
                } else
                {
                    throw new IllegalStateException("Warp " + dx + ", " + dy);
                }
            }
            previous.setCoordinates(it);
        });
        points.add(offset.toCoordinates());

        var isAlive = !snake.isZombie(); // will probably fix later
        var direction = snake.getDirection();

        return new Snake(playerId, points, isAlive, direction);
    }

    int getPlayerId();

    List<Coordinates> getPoints();

    boolean isAlive();

    Direction getDirection();

    static SnakeState fromMessage(final SnakesProto.GameState.SnakeOrBuilder snake)
    {
        var points = new ArrayList<Coordinates>();
        var result = new Snake(
                snake.getPlayerId(),
                points,
                snake.getState() == SnakesProto.GameState.Snake.SnakeState.ALIVE,
                Direction.fromNumber(snake.getHeadDirection().getNumber()));
        for (int i = 0; i < snake.getPointsCount(); i += 1)
        {
            var point = snake.getPoints(i);
            points.add(new UnboundedFixedPoint(point.getX(), point.getY()));
        }
        return result;
    }

    default SnakesProto.GameState.Snake toMessage()
    {
        var builder = SnakesProto.GameState.Snake.newBuilder()
                .setPlayerId(this.getPlayerId())
                .setState(
                        this.isAlive()
                                ? SnakesProto.GameState.Snake.SnakeState.ALIVE
                                : SnakesProto.GameState.Snake.SnakeState.ZOMBIE)
                .setHeadDirection(
                        SnakesProto.Direction.forNumber(this.getDirection().toNumber()));
        this.getPoints().forEach(it -> builder.addPoints(Utility.coordinates(it)));
        return builder.build();
    }
}
