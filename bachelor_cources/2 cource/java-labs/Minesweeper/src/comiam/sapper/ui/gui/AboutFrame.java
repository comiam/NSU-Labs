package comiam.sapper.ui.gui;

import comiam.sapper.ui.gui.components.UIDesigner;

import javax.swing.*;
import java.awt.*;

import static comiam.sapper.game.Minesweeper.GAME_VERSION;

public class AboutFrame
{
    public static void showAbout()
    {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIDesigner.DEFAULT_BACKGROUND);

        JLabel programNameL = new JLabel("Minesweeper v" + GAME_VERSION);
        programNameL.setFont(UIDesigner.getFont(18, programNameL.getFont(), false));
        programNameL.setForeground(Color.LIGHT_GRAY);
        content.add(programNameL);

        JLabel nameL = new JLabel("Designer: comiam.");
        nameL.setFont(UIDesigner.getFont(18, nameL.getFont(), false));
        nameL.setForeground(Color.LIGHT_GRAY);
        content.add(nameL);

        JLabel name2L = new JLabel("Developer: comiam.");
        name2L.setFont(UIDesigner.getFont(18, name2L.getFont(), false));
        name2L.setForeground(Color.LIGHT_GRAY);
        content.add(name2L);

        JLabel dateL = new JLabel("Date: 29.02.20.");
        dateL.setFont(UIDesigner.getFont(18, dateL.getFont(), false));
        dateL.setForeground(Color.LIGHT_GRAY);
        content.add(dateL);

        JFrame frame = new JFrame();
        frame.setContentPane(content);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle("About");
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
