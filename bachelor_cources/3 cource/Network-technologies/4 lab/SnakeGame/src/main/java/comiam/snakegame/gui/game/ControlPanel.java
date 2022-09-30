package comiam.snakegame.gui.game;

import comiam.snakegame.gui.util.Colours;

import javax.swing.*;
import java.awt.*;

final class ControlPanel extends JPanel
{

    ControlPanel(
            final GameWindow view,
            final SnakesPanel snakesPanel)
    {
        super(new GridLayout(3, 1));

        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Colours.DARK_LINING));

        var namesToggle = new JButton(snakesPanel.isShowingNames() ? "Hide names" : "Show names");
        namesToggle.setFocusPainted(false);
        namesToggle.addActionListener(unused -> {
            if (snakesPanel.isShowingNames())
            {
                namesToggle.setText("Show names");
                snakesPanel.doNotShowNames();
            } else
            {
                namesToggle.setText("Hide names");
                snakesPanel.showNames();
            }
        });
        this.add(namesToggle);

        var leaveButton = new JButton("Give up");
        leaveButton.setFocusPainted(false);
        leaveButton.addActionListener(unused -> {
            leaveButton.setEnabled(false);
            view.getLeaveHooks().forEach(Runnable::run);
        });
        this.add(leaveButton);
    }
}
