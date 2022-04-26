package comiam.snakegame.gui.game;

import comiam.snakegame.gamelogic.SnakesGameInfo;
import comiam.snakegame.gui.util.Colours;
import comiam.snakegame.gui.util.GuiUtils;

import javax.swing.*;
import java.awt.*;

final class GamePanel extends JPanel
{

    final SnakesPanel snakesPanel;
    final SidePanel sidePanel;

    GamePanel(
            final GameWindow view,
            final SnakesGameInfo gameState)
    {
        super(new BorderLayout());

        this.snakesPanel = new SnakesPanel(view, gameState);
        this.sidePanel = new SidePanel(view, gameState, this.snakesPanel);

        this.add(this.snakesPanel, BorderLayout.WEST);
        var sep = new JSeparator(SwingConstants.VERTICAL);
        GuiUtils.setColours(sep, Colours.LINING, Colours.BACKGROUND_COLOUR);
        this.add(sep, BorderLayout.CENTER);
        this.add(this.sidePanel, BorderLayout.EAST);
    }

    void update()
    {
        this.snakesPanel.repaint();
        this.sidePanel.scorePanel.updateScores();
    }
}
