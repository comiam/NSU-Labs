package comiam.snakegame.gui.game;

import comiam.snakegame.gamelogic.SnakesGameInfo;
import comiam.snakegame.gui.util.Colours;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;

public final class GameWindow extends JFrame implements SnakesGameView
{
    private final HashMap<Integer, Color> preferredColours = new HashMap<>();
    private final Deque<Runnable> exitHooks = new ArrayDeque<>();
    private final Deque<Runnable> leaveHooks = new ArrayDeque<>();
    private @Nullable GamePanel gamePanel;

    public GameWindow(final String name)
    {
        super(name);

        this.exitHooks.push(this::dispose);

        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent unused)
            {
                GameWindow.this.exitHooks.forEach(Runnable::run);
            }
        });
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
    }

    public GameWindow()
    {
        this("Y///uKu");
    }

    @Override
    public BiConsumer<Integer, Runnable> getKeyBindingsRegisterer()
    {
        return (keyCode, action) -> {
            var keyText = KeyEvent.getKeyText(keyCode);
            this.getRootPane()
                    .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(keyCode, 0), keyText);
            this.getRootPane()
                    .getActionMap()
                    .put(keyText, new AbstractAction()
                    {
                        @Override
                        public void actionPerformed(final @Nullable ActionEvent unused)
                        {
                            action.run();
                        }
                    });
        };
    }

    @Override
    public Consumer<Runnable> getExitHookRegisterer()
    {
        return this.exitHooks::push;
    }

    @Override
    public Consumer<Runnable> getLeaveHookRegisterer()
    {
        synchronized (this.leaveHooks)
        {
            return this.leaveHooks::push;
        }
    }

    @Override
    public void executeLeaveHooks()
    {
        synchronized (this.leaveHooks)
        {
            this.leaveHooks.forEach(Runnable::run);
        }
    }

    @Override
    public void makeVisible()
    {
        if (this.gamePanel == null)
        {
            throw new IllegalStateException("Not bound");
        }
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            this.gamePanel.snakesPanel.calculateScale();
        });
    }

    @Override
    public void updateView() throws IllegalStateException
    {
        if (this.gamePanel == null)
        {
            throw new IllegalStateException("Not bound");
        }
        this.gamePanel.update();
    }

    @Override
    public void bindTo(final SnakesGameInfo gameState)
    {
        var wasBound = this.gamePanel != null;
        if (wasBound)
        {
            this.getContentPane().removeAll();
        }
        this.gamePanel = new GamePanel(this, gameState);
        this.getContentPane().add(this.gamePanel);
        this.pack();
        this.gamePanel.snakesPanel.calculateScale();
        if (!wasBound)
        {
            this.setLocationRelativeTo(null);
        }
        this.repaint();
    }

    @Override
    public Color getColour(final int playerId)
    {
        synchronized (this.preferredColours)
        {
            return this.preferredColours.getOrDefault(playerId, Colours.LIGHT_GRAY);
        }
    }

    @Override
    public void setPreferredColour(
            final int playerId,
            final Color colour)
    {
        if (!(Colours.RED.equals(colour)
                || Colours.DEAD_SNAKE_COLOUR.equals(colour)
                || Colours.BACKGROUND_COLOUR.equals(colour)))
        {
            synchronized (this.preferredColours)
            {
                this.preferredColours.put(playerId, colour);
            }
        }
    }

    public Deque<Runnable> getLeaveHooks()
    {
        return this.leaveHooks;
    }
}
