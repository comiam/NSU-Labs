package comiam.snakegame.gui.menu;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gui.game.GameWindow;
import comiam.snakegame.gui.util.Colours;
import comiam.snakegame.gui.util.CustomScrollGUI;
import comiam.snakegame.gui.util.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

import static javax.swing.BoxLayout.Y_AXIS;

public class JoinPanel extends JPanel implements RunningGamesView
{

    private final Iterable<AddressedMessage> games;
    private final GameJoiner gameJoiner;
    private final JComponent mainList = new JPanel();
    private final MenuWindow window;

    JoinPanel(
            final MenuWindow window,
            final Iterable<AddressedMessage> games,
            final GameJoiner gameJoiner)
    {
        super(new BorderLayout());

        this.window = window;
        this.games = games;
        this.gameJoiner = gameJoiner;

        this.setPreferredSize(new Dimension(MenuWindow.MENU_PANEL_WIDTH, MenuWindow.MENU_PANEL_HEIGHT));
        GuiUtils.setColours(this, Colours.FOREGROUND_COLOUR, Colours.INTERFACE_BACKGROUND);

        var header = new JPanel(new GridLayout(1, 5));
        var hostName = new JLabel("Host name");
        hostName.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(hostName);
        var hostAddress = new JLabel("Host address");
        hostAddress.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(hostAddress);
        var planeSize = new JLabel("Plane size");
        planeSize.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(planeSize);
        var foodCount = new JLabel("Food count");
        foodCount.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(foodCount);
        var join = new JLabel("Join");
        join.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(join);

        var expectedSlideWidth = 20;
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, expectedSlideWidth));
        this.add(header, BorderLayout.NORTH);

        this.mainList.setLayout(new BoxLayout(this.mainList, Y_AXIS));
        this.mainList.setAlignmentY(TOP_ALIGNMENT);
        var scroll = new JScrollPane(
                this.mainList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        var scrollContainer = new JPanel(new BorderLayout());
        scrollContainer.add(scroll, BorderLayout.NORTH);
        this.add(scrollContainer, BorderLayout.CENTER);

        scroll.getVerticalScrollBar().setBackground(Colours.INTERFACE_BACKGROUND);
        scroll.getHorizontalScrollBar().setBackground(Colours.INTERFACE_BACKGROUND);
        scroll.getVerticalScrollBar().setUI(new CustomScrollGUI());
        scroll.setBorder(BorderFactory.createEmptyBorder());

        GuiUtils.setColours(scroll, Colours.LINING, Colours.INTERFACE_BACKGROUND);
    }

    @Override
    public void updateView()
    {
        this.mainList.removeAll();

        var entries = new ArrayList<AddressedMessage>();

        synchronized (this.games)
        {
            this.games.forEach(entries::add);
        }

        entries.sort(Comparator.comparingInt(it -> it.getMessage().getAnnouncement().getPlayers().getPlayersCount()));

        entries.forEach(it -> this.mainList.add(this.createGameEntry(it)));

        this.revalidate();
        this.repaint();
    }

    private JPanel createGameEntry(final AddressedMessage announcement)
    {
        var outer = new JPanel(new BorderLayout());
        var entry = new JPanel(new GridLayout(1, 5));

        var hostAddress = announcement.getAddress().getHostString();
        var contents = announcement.getMessage().getAnnouncement();

        var playersCount = contents.getPlayers().getPlayersCount();
        var config = contents.getConfig();
        var foodStatic = config.getFoodStatic();
        var foodPerPlayer = config.getFoodPerPlayer();
        var width = config.getWidth();
        var height = config.getHeight();

        var hostName = "???";
        for (int i = 0; i < playersCount; i += 1)
        {
            var player = contents.getPlayers().getPlayers(i);
            if (player.getIpAddress().isEmpty()
                    || player.getIpAddress().equals(hostAddress)
                    && player.getPort() == announcement.getAddress().getPort())
            {
                hostName = player.getName();
            }
        }

        var hostNameLabel = new JLabel(hostName);
        hostNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        entry.add(hostNameLabel);
        var address = new JLabel(hostAddress);
        address.setHorizontalAlignment(SwingConstants.CENTER);
        entry.add(address);
        var size = new JLabel(width + " x " + height);
        size.setHorizontalAlignment(SwingConstants.CENTER);
        entry.add(size);
        var food = new JLabel(String.format("%d + %.2f per player", foodStatic, foodPerPlayer));
        food.setHorizontalAlignment(SwingConstants.CENTER);
        entry.add(food);

        var joinButton = new JButton("Join");
        var finalHostName = hostName;

        joinButton.addActionListener(unused -> {
            joinButton.setEnabled(false);
            var gameView = new GameWindow();
            gameView.getExitHookRegisterer().accept(() -> {
                this.window.setVisible(true);
                joinButton.setEnabled(true);
            });
            this.gameJoiner.joinGame(
                    this.window.topPanel.getName(), Config.fromMessage(config),
                    announcement.getAddress(), gameView, () -> this.window.setVisible(false),
                    errorMessage -> {
                        JOptionPane.showMessageDialog(
                                this.window, errorMessage, "Cannot join " + finalHostName + "'s game",
                                JOptionPane.PLAIN_MESSAGE);
                        joinButton.setEnabled(true);
                    });
        });
        joinButton.setHorizontalAlignment(SwingConstants.CENTER);

        entry.add(joinButton);
        entry.setPreferredSize(new Dimension(entry.getPreferredSize().width, entry.getMinimumSize().height));

        outer.add(entry, BorderLayout.NORTH);
        outer.setPreferredSize(new Dimension(entry.getPreferredSize().width, outer.getMinimumSize().height));

        return outer;
    }
}
