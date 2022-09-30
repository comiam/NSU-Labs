package comiam.snakegame.gui.menu;

import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gui.game.SnakesGameView;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

@FunctionalInterface
public interface GameJoiner
{

    void joinGame(
            final String playerName,
            final Config gameConfig,
            final InetSocketAddress hostAddress,
            final SnakesGameView gameView,
            final Runnable onSuccess,
            final Consumer<String> onError);
}
