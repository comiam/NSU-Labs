package comiam.snakegame.gui.game;

import comiam.snakegame.gamelogic.SnakesGameInfo;
import comiam.snakegame.gui.util.Colours;
import comiam.snakegame.gui.util.CustomScrollGUI;
import comiam.snakegame.gui.util.GuiUtils;

import javax.swing.*;
import java.awt.*;

import static javax.swing.BoxLayout.Y_AXIS;

final class ScorePanel extends JPanel
{

    private final SnakesGameView view;
    private final SnakesGameInfo gameState;

    private final JComponent mainList = new JPanel();

    ScorePanel(
            final SnakesGameView view,
            final SnakesGameInfo gameState)
    {
        this.setLayout(new BorderLayout());

        this.view = view;
        this.gameState = gameState;

        var title = new JLabel("Score");
        title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colours.DARK_LINING));

        this.add(title, BorderLayout.NORTH);
        this.mainList.setLayout(new BoxLayout(this.mainList, Y_AXIS));
        this.mainList.setAlignmentY(TOP_ALIGNMENT);
        var scroll = new JScrollPane(
                this.mainList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        this.add(scroll, BorderLayout.CENTER);

        scroll.getVerticalScrollBar().setBackground(Colours.INTERFACE_BACKGROUND);
        scroll.getHorizontalScrollBar().setBackground(Colours.INTERFACE_BACKGROUND);
        scroll.getVerticalScrollBar().setUI(new CustomScrollGUI());
        scroll.setBorder(BorderFactory.createEmptyBorder());

        GuiUtils.setColours(scroll, Colours.LINING, Colours.INTERFACE_BACKGROUND);
    }

    void updateScores()
    {
        SwingUtilities.invokeLater(this::doUpdateScore);
    }

    private void doUpdateScore()
    {
        this.mainList.removeAll();
        var no = new int[]{1};
        synchronized (this.gameState)
        {
            this.gameState.forEachPlayer(it -> {
                if (this.gameState.hasSnakeWithPlayerId(it.getId()))
                {
                    var snake = this.gameState.getSnakeById(it.getId());
                    if (snake.isZombie())
                    {
                        return;
                    }
                    var scoreEntry = new JPanel(new BorderLayout());

                    var nameAndNo = new JPanel();

                    SidePanel.setMaxSizeOf(nameAndNo);

                    var score = new JLabel(it.getScore() + " "); // hell yeah DeSiGn
                    SidePanel.setMaxSizeOf(score);
                    score.setAlignmentX(RIGHT_ALIGNMENT);

                    scoreEntry.add(nameAndNo, BorderLayout.WEST);
                    scoreEntry.add(score, BorderLayout.EAST);

                    SidePanel.setMaxSizeOf(scoreEntry);

                    this.mainList.add(scoreEntry);

                    no[0] += 1;
                }
            });
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize()
    {
        var def = super.getPreferredSize();
        return new Dimension(SidePanel.PREFERRED_WIDTH, def.height);
    }
}
