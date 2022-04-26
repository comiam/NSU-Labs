package comiam.snakegame.gui.game;

import comiam.snakegame.gamelogic.SnakesGameInfo;
import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SnakesGameView
{
    @Contract(mutates = "this")
    void makeVisible();

    /**
     * Render any changes happened to the model this view is bound to
     *
     * @throws IllegalStateException if not bound to any model
     */
    @Contract(mutates = "this")
    void updateView() throws IllegalStateException;

    /**
     * Bind this view to a model
     *
     * @param gameState game model to bind to
     */
    @Contract(mutates = "this")
    void bindTo(final SnakesGameInfo gameState);

    @Contract(mutates = "this")
    void setPreferredColour(final int playerId, final Color color);

    Color getColour(final int playerId);

    BiConsumer<Integer, Runnable> getKeyBindingsRegisterer();

    Consumer<Runnable> getExitHookRegisterer();

    Consumer<Runnable> getLeaveHookRegisterer();

    void executeLeaveHooks();
}
