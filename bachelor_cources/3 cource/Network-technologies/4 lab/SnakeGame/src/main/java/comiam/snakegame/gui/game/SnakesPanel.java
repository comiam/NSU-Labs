package comiam.snakegame.gui.game;

import comiam.snakegame.gamelogic.gamefield.BoundedMovablePoint;
import comiam.snakegame.gamelogic.gamefield.BoundedPoint;
import comiam.snakegame.gui.util.Colours;
import comiam.snakegame.gamelogic.SnakesGameInfo;
import comiam.snakegame.gui.util.GuiUtils;
import comiam.snakegame.gamelogic.snake.SnakeInfo;
import me.ippolitov.fit.snakes.SnakesProto;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SnakesPanel extends JPanel
{
    private static final int MAX_HEIGHT = 600;
    private static final int MAX_WIDTH = 800;

    private static final double MID_HEAD_SQUARE_SCALE = 0.4;
    private static final int MID_HEAD_SQUARE_ARC = 3;
    private static final double SQUARE_SCALE = 0.8;
    private static final int SQUARE_ARC = 4;

    private static final Logger logger = Logger.getLogger(SnakesPanel.class.getSimpleName());

    private final SnakesGameView view;
    private final SnakesGameInfo gameState;

    private double scaleX;
    private double scaleY;

    private Graphics2D g2d;

    private @Nullable Integer focusOnId;
    private @Nullable BoundedMovablePoint center;

    private boolean mustShowNames = false;

    SnakesPanel(
            final SnakesGameView view,
            final SnakesGameInfo gameState)
    {
        super();

        this.view = view;
        this.gameState = gameState;

        GuiUtils.setColours(this, Colours.FOREGROUND_COLOUR, Colours.GAME_PANEL_BACKGROUND);
        this.recalculatePreferredSize();
    }

    void calculateScale()
    {
        this.scaleX = ((double) this.getWidth() / this.gameState.getPlaneBounds().getX());
        this.scaleY = ((double) this.getHeight() / this.gameState.getPlaneBounds().getY());
    }

    void showNames()
    {
        this.mustShowNames = true;
        this.repaint();
    }

    void doNotShowNames()
    {
        this.mustShowNames = false;
        this.repaint();
    }

    boolean isShowingNames()
    {
        return this.mustShowNames;
    }

    void focusOn(final int playerId)
    {
        this.focusOnId = playerId;
    }

    @Nullable Integer getFocusOnId()
    {
        return this.focusOnId;
    }

    void unfocus()
    {
        this.focusOnId = null;
    }

    private void recalculatePreferredSize()
    {
        var width = this.gameState.getPlaneBounds().getX();
        var height = this.gameState.getPlaneBounds().getY();

        if (width > height)
        {
            var scale = ((double) MAX_WIDTH) / width;
            if (scale * height <= MAX_HEIGHT)
            {
                this.setPreferredSize(new Dimension(MAX_WIDTH, (int) (scale * height)));
            } else
            {
                var newScale = ((double) MAX_HEIGHT) / (scale * height);
                this.setPreferredSize(new Dimension((int) (newScale * MAX_WIDTH), (int) (newScale * scale * height)));
            }
        } else
        {
            var scale = ((double) MAX_HEIGHT) / height;
            // Vertical monitors are not supported
            this.setPreferredSize(new Dimension((int) (scale * width), MAX_HEIGHT));
        }
    }

    void paintSnake(
            final SnakeInfo snake)
    {
        var prevColour = this.g2d.getColor();
        this.g2d.setColor(this.view.getColour(snake.getPlayerId()));

        var point = new BoundedMovablePoint(0, 0, this.gameState.getPlaneBounds());
        snake.forEachSegment(it -> {
            point.setCoordinates(it);
            this.centerPoint(point);
            var x = point.getX() * this.scaleX;
            var y = point.getY() * this.scaleY;

            if (it.equals(snake.getHead()))
            {
                this.g2d.drawRoundRect(
                        (int) (x + this.scaleX * (1.0 - MID_HEAD_SQUARE_SCALE) / 2),
                        (int) (y + this.scaleY * (1.0 - MID_HEAD_SQUARE_SCALE) / 2),
                        (int) (this.scaleX * MID_HEAD_SQUARE_SCALE), (int) (this.scaleY * MID_HEAD_SQUARE_SCALE),
                        MID_HEAD_SQUARE_ARC, MID_HEAD_SQUARE_ARC);
            }
            this.g2d.drawRoundRect(
                    (int) (x + this.scaleX * (1.0 - SQUARE_SCALE) / 2),
                    (int) (y + this.scaleY * (1.0 - SQUARE_SCALE) / 2),
                    (int) (this.scaleX * SQUARE_SCALE), (int) (this.scaleY * SQUARE_SCALE),
                    SQUARE_ARC, SQUARE_ARC);
        });

        this.g2d.setColor(prevColour);

        if (this.mustShowNames)
        {
            this.paintName(snake.getPlayerId(), snake.getHead());
        }
    }

    private void paintName(
            final int playerId,
            final BoundedPoint where)
    {
        var point = new BoundedMovablePoint(where, this.gameState.getPlaneBounds());
        this.centerPoint(point);
        var x = (int) (point.getX() * this.scaleX);
        var y = (int) (point.getY() * this.scaleY);

        var player = this.gameState.getPlayerById(playerId);
        var snake = this.gameState.getSnakeById(playerId);

        var displayName = GuiUtils.trimNameToFitMaxLength(
                player.getName(),
                player.getType() == SnakesProto.PlayerType.ROBOT,
                snake.isZombie());

        this.paintName(displayName, x, y);
    }

    private void paintName(
            final String name,
            final int x,
            final int y)
    {
        var prevColour = this.g2d.getColor();

        this.g2d.setColor(Color.DARK_GRAY);
        // well if you can suggest something "better" and not 100+ lines long i'm all ears
        this.g2d.drawString(name, x - 1, y - 1);
        this.g2d.drawString(name, x + 1, y + 1);
        this.g2d.drawString(name, x + 1, y - 1);
        this.g2d.drawString(name, x - 1, y + 1);

        this.g2d.setColor(Colours.NAME_COLOUR);
        this.g2d.drawString(name, x, y);

        this.g2d.setColor(prevColour);
    }

    private void paintFood(final BoundedPoint foodPosition)
    {
        var prevColour = this.g2d.getColor();
        this.g2d.setColor(Colours.RED);

        var point = new BoundedMovablePoint(foodPosition, this.gameState.getPlaneBounds());
        this.centerPoint(point);
        var x = point.getX() * this.scaleX;
        var y = point.getY() * this.scaleY;

        this.g2d.drawRoundRect(
                (int) (x + this.scaleX * (1.0 - SQUARE_SCALE) / 2),
                (int) (y + this.scaleY * (1.0 - SQUARE_SCALE) / 2),
                (int) (this.scaleX * SQUARE_SCALE), (int) (this.scaleY * SQUARE_SCALE),
                SQUARE_ARC, SQUARE_ARC);

        this.g2d.setColor(prevColour);
    }

    private void centerPoint(final BoundedPoint point)
    {
        if (this.focusOnId != null)
        {
            if (this.gameState.hasSnakeWithPlayerId(this.focusOnId))
            {
                if (this.center == null)
                {
                    this.center = new BoundedMovablePoint(
                            this.gameState.getSnakeById(this.focusOnId).getHead(),
                            this.gameState.getSnakeById(this.focusOnId).getHead().getBounds());
                } else
                {
                    this.center.setCoordinates(this.gameState.getSnakeById(this.focusOnId).getHead());
                }
            } else
            {
                this.focusOnId = null;
            }
        }
        if (this.center != null)
        {
            point.centerRelativeTo(this.center);
        }
    }

    @Override
    public void paintComponent(final Graphics g)
    {
        this.g2d = (Graphics2D) g;
        this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.g2d.setStroke(new BasicStroke(2));
        super.paintComponent(g);

        var start = System.nanoTime();
        synchronized (this.gameState)
        {
            this.gameState.forEachFood(this::paintFood);
            var food = System.nanoTime();
            this.gameState.forEachSnake(this::paintSnake);
            var snakes = System.nanoTime();
            logger.log(Level.FINEST, "snakes and names {0} | food {1}",
                    new Object[]{
                            (snakes - food) / 1_000_000,
                            (food - start) / 1_000_000});
        }
    }
}

