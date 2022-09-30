package comiam.snakegame.gui.game;

import comiam.snakegame.gamelogic.SnakesGameInfo;

import javax.swing.*;
import java.awt.*;

final class SidePanel extends JPanel
{

    static final int PREFERRED_WIDTH = 120;
    final ScorePanel scorePanel;

    SidePanel(
            final GameWindow view,
            final SnakesGameInfo gameState,
            final SnakesPanel snakesPanel)
    {
        super(new BorderLayout());

        this.scorePanel = new ScorePanel(view, gameState);
        this.add(this.scorePanel, BorderLayout.CENTER);
        this.add(new ControlPanel(view, snakesPanel), BorderLayout.SOUTH);

        setMaxSizeOf(this);
    }

    static void setMaxSizeOf(final JComponent component)
    {
        component.setMaximumSize(new Dimension(PREFERRED_WIDTH, component.getMinimumSize().height));
    }
}
