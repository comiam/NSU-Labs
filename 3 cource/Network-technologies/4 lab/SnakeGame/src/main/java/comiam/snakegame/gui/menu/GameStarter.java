package comiam.snakegame.gui.menu;

import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gui.game.SnakesGameView;

@FunctionalInterface
public interface GameStarter
{

    void startGame(
            final String playerName,
            final Config gameConfig,
            final SnakesGameView gameView);
}
