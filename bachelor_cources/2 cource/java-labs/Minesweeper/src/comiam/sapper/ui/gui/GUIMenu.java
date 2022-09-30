package comiam.sapper.ui.gui;

import comiam.sapper.game.Minesweeper;
import comiam.sapper.ui.gui.components.CustomButton;
import comiam.sapper.ui.gui.components.UIDesigner;
import comiam.sapper.ui.tui.TextGame;

import javax.swing.*;
import java.awt.*;

import static comiam.sapper.ui.gui.components.CustomDialog.getDimension;
import static comiam.sapper.ui.gui.components.UIDesigner.DEFAULT_BACKGROUND;

public class GUIMenu extends JFrame
{
    private static JFrame mainFrame;
    private JPanel mainPanel;
    private boolean twoInterfacesNeed = false;

    public GUIMenu(boolean twoInterfacesNeed)
    {
        UIDesigner.init();
        if(mainFrame != null)
            return;

        this.twoInterfacesNeed = twoInterfacesNeed;
        setupUI();
        setContentPane(getRootComponent());
        setSize(180, 240);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame = this;
    }

    private void setupUI()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(DEFAULT_BACKGROUND);

        CustomButton newGameButton = new CustomButton();
        newGameButton.setText("New game");
        newGameButton.addActionListener(e -> {
            Minesweeper.FieldDimension dim = getDimension(mainFrame);
            if(dim == Minesweeper.FieldDimension.nothing)
                return;

            openGame(dim);
        });

        Font label12Font = UIDesigner.getFont(15, newGameButton.getFont(), false);
        if(label12Font != null) newGameButton.setFont(label12Font);

        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(newGameButton, gbc);

        CustomButton highScoresButton = new CustomButton();
        highScoresButton.setText("High Scores");
        label12Font = UIDesigner.getFont(15, newGameButton.getFont(), false);
        if(label12Font != null) highScoresButton.setFont(label12Font);
        highScoresButton.addActionListener((e) -> ScoresFrame.showRecords(this));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(highScoresButton, gbc);

        CustomButton aboutButton = new CustomButton();
        aboutButton.setText("About");
        aboutButton.addActionListener((e) -> AboutFrame.showAbout());
        label12Font = UIDesigner.getFont(15, aboutButton.getFont(), false);
        if(label12Font != null) aboutButton.setFont(label12Font);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(aboutButton, gbc);

        CustomButton exitButton = new CustomButton();
        exitButton.setText("Exit");
        label12Font = UIDesigner.getFont(15, exitButton.getFont(), false);
        if(label12Font != null) exitButton.setFont(label12Font);
        exitButton.addActionListener(e -> System.exit(0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(exitButton, gbc);

        final JLabel label1 = new JLabel();
        Font label1Font = UIDesigner.getFont(22, label1.getFont(), false);
        if(label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("Minesweeper");
        label1.setVerticalAlignment(1);
        label1.setVerticalTextPosition(1);
        label1.setForeground(Color.LIGHT_GRAY);

        mainPanel.add(label1);

        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.01;
        spacer1.setBackground(DEFAULT_BACKGROUND);
        mainPanel.add(spacer1, gbc);
    }

    private void openGame(Minesweeper.FieldDimension dim)
    {
        Minesweeper.newGame(dim);

        GUIGame cont = new GUIGame();
        Minesweeper.setMainController(cont);
        cont.init(this);

        if(twoInterfacesNeed)
        {
            TextGame tg = new TextGame();
            Minesweeper.addController(tg);
            tg.init(false);
        }
    }
    private JComponent getRootComponent()
    {
        return mainPanel;
    }
}
