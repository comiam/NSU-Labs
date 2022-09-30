package comiam.snakegame.gamelogic;

import comiam.snakegame.gamelogic.gameobjects.player.PlayerInfo;
import comiam.snakegame.gamelogic.gamefield.BoundedPoint;
import comiam.snakegame.gamelogic.gameobjects.game.GameState;
import comiam.snakegame.gamelogic.gamefield.Coordinates;
import comiam.snakegame.gamelogic.snake.SnakeInfo;
import org.jetbrains.annotations.Contract;

import java.util.function.Consumer;

public interface SnakesGameInfo
{

    void forEachSnake(final Consumer<SnakeInfo> action);

    SnakeInfo getSnakeById(final int playerId);

    void forEachFood(final Consumer<BoundedPoint> action);

    boolean isFood(final BoundedPoint point);

    void forEachPlayer(final Consumer<PlayerInfo> action);

    PlayerInfo getPlayerById(final int playerId);

    boolean hasPlayerWithId(final int playerId);

    boolean hasSnakeWithPlayerId(final int playerId);

    @Contract(pure = true)
    Coordinates getPlaneBounds();

    GameState getState();

    int getStateOrder();
}
