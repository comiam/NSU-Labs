package comiam.snakegame.gamelogic.gameobjects.game;

import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gamelogic.gameobjects.player.PlayerInfo;
import comiam.snakegame.gamelogic.gameobjects.snake.SnakeState;
import comiam.snakegame.gamelogic.gamefield.Coordinates;

public class Game implements GameState
{
    private int stateOrder;
    private final Iterable<SnakeState> snakes;
    private final Iterable<Coordinates> food;
    private final Iterable<PlayerInfo> players;
    private final Config config;

    public Game(
            final int stateOrder,
            final Iterable<SnakeState> snakes,
            final Iterable<Coordinates> food,
            final Iterable<PlayerInfo> players,
            final Config config)
    {
        this.stateOrder = stateOrder;
        this.snakes = snakes;
        this.food = food;
        this.players = players;
        this.config = config;
    }

    public void incrementStateOrder()
    {
        this.stateOrder += 1;
    }

    public void setStateOrder(final int stateOrder)
    {
        this.stateOrder = stateOrder;
    }

    public int getStateOrder()
    {
        return this.stateOrder;
    }

    public Iterable<SnakeState> getSnakes()
    {
        return this.snakes;
    }

    public Iterable<Coordinates> getFood()
    {
        return this.food;
    }

    public Iterable<PlayerInfo> getPlayers()
    {
        return this.players;
    }

    public Config getConfig()
    {
        return this.config;
    }

    public String toString()
    {
        return "GameDescriptor(stateOrder=" + this.getStateOrder() + ", snakes=" + this.getSnakes() + ", food=" + this.getFood() + ", players=" + this.getPlayers() + ", config=" + this.getConfig() + ")";
    }
}
