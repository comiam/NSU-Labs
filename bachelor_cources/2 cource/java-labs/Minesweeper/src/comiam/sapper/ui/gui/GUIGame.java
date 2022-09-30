package comiam.sapper.ui.gui;

import comiam.sapper.game.Minesweeper;
import comiam.sapper.game.records.Pair;
import comiam.sapper.game.records.ScoreRecords;
import comiam.sapper.ui.GameViewController;
import comiam.sapper.ui.gui.components.CustomButton;
import comiam.sapper.ui.gui.components.CustomDialog;
import comiam.sapper.ui.gui.components.CustomPanel;
import comiam.sapper.ui.gui.components.UIDesigner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;

import static comiam.sapper.time.Timer.*;
import static comiam.sapper.ui.gui.components.UIDesigner.DEFAULT_BACKGROUND;
import static comiam.sapper.util.TextUtils.getTimeString;

public class GUIGame extends JPanel implements GameViewController
{
    private static final int SIZE = 30;
    private final HashMap<CustomButton, Image> imageMap = new HashMap<>();
    private Image bomb;
    private Image flag;
    private Image goodFlag;
    private Image badFlag;
    private Image flagMaybe;
    private Image goodFlagMaybe;
    private Image badFlagMaybe;
    private CustomPanel minePanel;
    private CustomPanel pausePanel;
    private JPanel statPanel;
    private JLabel flagCountL;
    private JLabel timerCountL;
    private CustomButton pause;
    private CustomButton replay;
    private CustomButton[] cells;

    private boolean isButtonStop = false;
    private JFrame parent;

    public void init(JFrame parent)
    {
        UIDesigner.init();
        if(parent == null)
        {
            this.parent = new JFrame();
            this.parent.setSize(300, 240);
            this.parent.setLocationRelativeTo(null);
            this.parent.setDefaultCloseOperation(Minesweeper.isMainController(this) ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
            this.parent.setVisible(true);
        } else
            this.parent = parent;

        makeFrameListeners();
        setLayout(new GridBagLayout());
        this.parent.setContentPane(this);

        setSizes();
        fillContent();
        loadContent();
        setAutoResizer();
        updatePanel();
        this.parent.setLocationRelativeTo(null);

        if(parent == null)
        {
            this.parent.revalidate();
            this.parent.repaint();
        }
    }

    @Override
    public Dimension getMinimumSize()
    {
        int width = minePanel.getMinimumSize().width + statPanel.getMinimumSize().width;
        int height = minePanel.getMinimumSize().height;
        return new Dimension(width, height);
    }

    @Override
    public Dimension getPreferredSize()
    {
        int width = minePanel.getMinimumSize().width + statPanel.getMinimumSize().width;
        int height = minePanel.getMinimumSize().height;
        return new Dimension(width, height);
    }

    private void setAutoResizer()
    {
        for(JButton b : cells)
        {
            Font label12Font = UIDesigner.getFont(22, b.getFont(), true);
            if(label12Font != null) b.setFont(label12Font);
            b.addComponentListener(new ComponentAdapter()
            {
                protected void decreaseFontSize(JButton comp)
                {
                    Font font = comp.getFont();
                    FontMetrics fm = comp.getFontMetrics(font);
                    int width = comp.getWidth();
                    int height = comp.getHeight();
                    int textWidth = fm.stringWidth(comp.getText());
                    int textHeight = fm.getHeight();

                    int size = font.getSize();
                    while(size > 0 && (textHeight - 10 > height || textWidth - 10 > width))
                    {
                        size -= 2;
                        font = font.deriveFont(font.getStyle(), size);
                        fm = comp.getFontMetrics(font);
                        textWidth = fm.stringWidth(comp.getText());
                        textHeight = fm.getHeight();
                    }

                    comp.setFont(font);
                }

                protected void increaseFontSize(JButton comp)
                {
                    Font font = comp.getFont();
                    FontMetrics fm = comp.getFontMetrics(font);
                    int width = comp.getWidth();
                    int height = comp.getHeight();
                    int textWidth = fm.stringWidth(comp.getText());
                    int textHeight = fm.getHeight();

                    int size = font.getSize();
                    while(textHeight - 25 < height && textWidth - 25 < width)
                    {
                        size += 2;
                        font = font.deriveFont(font.getStyle(), size);
                        fm = comp.getFontMetrics(font);
                        textWidth = fm.stringWidth(comp.getText());
                        textHeight = fm.getHeight();
                    }

                    comp.setFont(font);
                    decreaseFontSize(comp);
                }

                @Override
                public void componentResized(ComponentEvent e)
                {
                    JButton comp = (JButton) e.getComponent();
                    Font font = comp.getFont();
                    FontMetrics fm = comp.getFontMetrics(font);
                    int width = comp.getWidth();
                    int height = comp.getHeight();
                    int textWidth = fm.stringWidth(comp.getText());
                    int textHeight = fm.getHeight();

                    int offset;
                    if(textHeight > height || textWidth > width)
                    {
                        decreaseFontSize(comp);
                        offset = (int) (width * 0.15);
                    } else
                    {
                        increaseFontSize(comp);
                        offset = (int) (width * 0.2);
                    }

                    if(imageMap.containsKey(comp))
                    {
                        comp.setIcon(new ImageIcon(imageMap.get(comp).getScaledInstance(width - offset, height - offset, Image.SCALE_AREA_AVERAGING)));
                        if(!imageMap.get(comp).equals(bomb))
                            comp.setDisabledIcon(comp.getIcon());
                    }
                }
            });
        }
    }

    private void setSizes()
    {
        setPreferredSize(new Dimension(Minesweeper.getFieldSize().width * SIZE, Minesweeper.getFieldSize().height * SIZE));
        setSize(new Dimension(Minesweeper.getFieldSize().width * SIZE, Minesweeper.getFieldSize().height * SIZE));
        setToMinimum();
    }

    private void loadContent()
    {
        try
        {
            bomb = ImageIO.read(GUIGame.class.getResourceAsStream("/res/mine.png"));
            flag = ImageIO.read(GUIGame.class.getResourceAsStream("/res/flag.png"));
            flagMaybe = ImageIO.read(GUIGame.class.getResourceAsStream("/res/flagmaybe.png"));
            goodFlag = ImageIO.read(GUIGame.class.getResourceAsStream("/res/goodflag.png"));
            badFlag = ImageIO.read(GUIGame.class.getResourceAsStream("/res/badflag.png"));
            goodFlagMaybe = ImageIO.read(GUIGame.class.getResourceAsStream("/res/goodflagmaybe.png"));
            badFlagMaybe = ImageIO.read(GUIGame.class.getResourceAsStream("/res/badflagmaybe.png"));
        } catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "I cant load images. Game is krek :c", "Error!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void fillContent()
    {
        this.removeAll();
        this.setBackground(DEFAULT_BACKGROUND);
        minePanel = new CustomPanel();
        isButtonStop = false;

        minePanel.setPreferredSize(new Dimension(Minesweeper.getFieldSize().width * SIZE, Minesweeper.getFieldSize().height * SIZE));
        minePanel.setMinimumSize(new Dimension(Minesweeper.getFieldSize().width * SIZE, Minesweeper.getFieldSize().height * SIZE));
        minePanel.setLayout(new GridLayout(Minesweeper.getFieldSize().width, Minesweeper.getFieldSize().height, 0, 0));
        minePanel.setBackground(DEFAULT_BACKGROUND);

        cells = new CustomButton[Minesweeper.getFieldSize().width * Minesweeper.getFieldSize().height];

        for(int i = 0; i < Minesweeper.getFieldSize().width * Minesweeper.getFieldSize().height; i++)
        {
            CustomButton btn = new CustomButton();
            final int finalI = i;

            if(Minesweeper.isMainController(this))
                btn.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        if(!e.getComponent().isEnabled())
                            return;
                        if(!Minesweeper.isGameStarted())
                        {
                            Minesweeper.initField(finalI % Minesweeper.getFieldSize().width, finalI / Minesweeper.getFieldSize().height);
                            Minesweeper.startGame();
                            pause.setVisible(true);
                            replay.setVisible(true);
                            turnOnTimer();
                        }

                        if(SwingUtilities.isLeftMouseButton(e))
                            Minesweeper.openCell(finalI % Minesweeper.getFieldSize().width, finalI / Minesweeper.getFieldSize().height);
                        else if(SwingUtilities.isRightMouseButton(e))
                            Minesweeper.markCell(finalI % Minesweeper.getFieldSize().width, finalI / Minesweeper.getFieldSize().height);
                    }
                });
            minePanel.add(btn);
            if(!Minesweeper.isMainController(this))
                btn.setEnabled(false);
            cells[i] = btn;
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;

        add(minePanel, c);

        statPanel = new JPanel();
        statPanel.setLayout(new BorderLayout());
        statPanel.setBackground(DEFAULT_BACKGROUND);
        statPanel.setMinimumSize(new Dimension(120, Minesweeper.getFieldSize().height * SIZE));
        statPanel.setPreferredSize(new Dimension(120, Minesweeper.getFieldSize().height * SIZE));

        JPanel stat0Panel = new JPanel();
        stat0Panel.setLayout(new BoxLayout(stat0Panel, BoxLayout.Y_AXIS));
        stat0Panel.setBackground(DEFAULT_BACKGROUND);
        stat0Panel.setPreferredSize(new Dimension(125, 170));

        flagCountL = new JLabel();
        try
        {
            flagCountL.setIcon(new ImageIcon(ImageIO.read(GUIGame.class.getResourceAsStream("/res/flag.png")).getScaledInstance(40, 50, Image.SCALE_AREA_AVERAGING)));
        } catch(IOException ignored)
        {
        }

        flagCountL.setText("");
        flagCountL.setForeground(Color.LIGHT_GRAY);
        flagCountL.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagCountL.setHorizontalTextPosition(JLabel.CENTER);
        flagCountL.setVerticalTextPosition(JLabel.BOTTOM);
        flagCountL.setMaximumSize(new Dimension(flagCountL.getMaximumSize().width + 10, 80));
        stat0Panel.add(flagCountL);

        timerCountL = new JLabel();
        try
        {
            timerCountL.setIcon(new ImageIcon(ImageIO.read(GUIGame.class.getResourceAsStream("/res/timer.png")).getScaledInstance(50, 50, Image.SCALE_AREA_AVERAGING)));
        } catch(IOException ignored)
        {
        }

        timerCountL.setText("00:00:00");
        timerCountL.setForeground(Color.LIGHT_GRAY);
        timerCountL.setHorizontalTextPosition(JLabel.CENTER);
        timerCountL.setVerticalTextPosition(JLabel.BOTTOM);
        timerCountL.setAlignmentX(Component.CENTER_ALIGNMENT);
        stat0Panel.add(timerCountL);

        statPanel.add(stat0Panel, BorderLayout.NORTH);

        JPanel stat1Panel = new JPanel();
        stat1Panel.setLayout(new BoxLayout(stat1Panel, BoxLayout.Y_AXIS));
        stat1Panel.setBackground(DEFAULT_BACKGROUND);
        stat1Panel.setPreferredSize(new Dimension(120, 90));

        CustomButton newGame = new CustomButton("New game?");
        newGame.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        newGame.addActionListener(e -> Minesweeper.rebuildControllers());

        Font label12Font = UIDesigner.getFont(15, newGame.getFont(), false);
        if(label12Font != null) newGame.setFont(label12Font);
        stat1Panel.add(newGame);

        replay = new CustomButton("Replay?");
        replay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        replay.addActionListener(e -> Minesweeper.restartControllers());

        label12Font = UIDesigner.getFont(15, replay.getFont(), false);
        if(label12Font != null) replay.setFont(label12Font);
        stat1Panel.add(replay);

        pause = new CustomButton("Pause?");
        pause.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        pause.addActionListener(e ->
        {
            if(Minesweeper.isGameEnded())
                return;

            Minesweeper.pauseControllers();
            setPause();
        });

        label12Font = UIDesigner.getFont(15, pause.getFont(), false);
        if(label12Font != null) pause.setFont(label12Font);
        stat1Panel.add(pause);

        statPanel.add(stat1Panel, BorderLayout.SOUTH);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;

        add(statPanel, c);
        repaintFlag();

        if(!Minesweeper.isMainController(this))
            newGame.setVisible(false);
        pause.setVisible(false);
        replay.setVisible(false);
    }

    public void repaintFlag()
    {
        flagCountL.setText(Minesweeper.getFlagCount() + "/" + Minesweeper.getMaxFlagCount());
    }

    @Override
    public boolean restartGame()
    {
        if(Minesweeper.isMainController(this))
        {
            if(!Minesweeper.isGameEnded() && Minesweeper.isGameStarted())
            {
                int a = JOptionPane.showConfirmDialog(parent,
                        "<html><h2>You are sure?</h2><i>Do you want start a new game?</i>",
                        "Message",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if(a != JOptionPane.YES_OPTION)
                {
                    this.grabFocus();
                    return false;
                }
            }
        }

        if(!isRunning() && pausePanel != null)
        {
            GridBagConstraints c0 = new GridBagConstraints();
            c0.gridx = 0;
            c0.gridy = 0;
            c0.fill = GridBagConstraints.BOTH;
            c0.anchor = GridBagConstraints.CENTER;

            pause.setText("Pause?");
            remove(pausePanel);
            pausePanel = null;
            add(minePanel, c0);
        }

        for(var b : cells)
        {
            b.setBackground(UIDesigner.BUTTON_BACKGROUND);
            b.setText("");
            b.setIcon(null);
            b.setDisabledIcon(null);
            b.setEnabled(Minesweeper.isMainController(this));
            imageMap.remove(b);
        }
        if(Minesweeper.isMainController(this))
            Minesweeper.newGame();
        pause.setVisible(false);
        timerCountL.setText("00:00:00");
        isButtonStop = false;

        revalidate();
        repaint();
        repaintFlag();

        return true;
    }

    @Override
    public boolean rebuildField()
    {
        if(Minesweeper.isMainController(this))
        {
            if(!Minesweeper.isGameEnded() && Minesweeper.isGameStarted())
            {
                int a = JOptionPane.showConfirmDialog(parent,
                        "<html><h2>You are sure?</h2><i>Do you want start a new game?</i>",
                        "Message",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if(a != JOptionPane.YES_OPTION)
                {
                    this.grabFocus();
                    return false;
                }
            }
            Minesweeper.FieldDimension dim = CustomDialog.getDimension(parent);
            if(dim == Minesweeper.FieldDimension.nothing)
                return false;
            Minesweeper.newGame(dim);
        }

        setSizes();
        fillContent();
        setAutoResizer();
        updatePanel();
        isButtonStop = false;
        pause.setVisible(false);
        timerCountL.setText("00:00:00");
        repaintFlag();

        return true;
    }

    @Override
    public void noticeOverGame()
    {
        if(Minesweeper.isMainController(this))
            JOptionPane.showMessageDialog(parent, "<html><h2>Sorry, you was died :c</h2><i>play again!</i>");
    }

    @Override
    public void noticeWinGame()
    {
        if(Minesweeper.isMainController(this))
        {
            JOptionPane.showMessageDialog(parent, "<html><h2>You win! с:</h2><i>Your score " +
                    getTimeString(getSeconds() / 3600) + ":" +
                    getTimeString((getSeconds() % 3600) / 60) + ":" +
                    getTimeString(getSeconds() % 60) + "!</i>");

            String name;
            do
                name = (String) JOptionPane.showInputDialog(parent, "Pls, enter your name :)\nDon't use ';' and names with length mre than 10! >:З", "Message", JOptionPane.QUESTION_MESSAGE, null, null, System.getProperty("user.name") == null ? "user" : System.getProperty("user.name"));
            while(name == null || name.contains(";") || name.length() > 10);

            Pair p = new Pair(name + ";" + getTimeString(getSeconds() / 3600) + ":" +
                    getTimeString((getSeconds() % 3600) / 60) + ":" +
                    getTimeString(getSeconds() % 60));
            ScoreRecords.saveRecord(p);
            ScoresFrame.showRecords(parent);
        }
    }

    @Override
    public boolean isGUI()
    {
        return true;
    }

    @Override
    public void update(boolean makeOnlyOutSymbol)
    {
    }

    private CustomPanel createPausePanel()
    {
        CustomPanel blockPanel = new CustomPanel();
        blockPanel.setBackground(Color.GRAY);
        blockPanel.setPreferredSize(minePanel.getSize());
        blockPanel.setMinimumSize(minePanel.getMinimumSize());
        blockPanel.setLayout(new BorderLayout());

        JLabel pause = new JLabel("Pause");
        pause.setForeground(Color.LIGHT_GRAY);
        pause.setHorizontalTextPosition(JLabel.CENTER);
        pause.setHorizontalAlignment(JLabel.CENTER);
        pause.setFont(UIDesigner.getFont(28, pause.getFont(), false));
        pause.addComponentListener(new ComponentAdapter()
        {
            protected void decreaseFontSize(JLabel comp)
            {
                Font font = comp.getFont();
                FontMetrics fm = comp.getFontMetrics(font);
                int width = comp.getWidth();
                int height = comp.getHeight();
                int textWidth = fm.stringWidth(comp.getText());
                int textHeight = fm.getHeight();

                int size = font.getSize();
                while(size > 0 && (textHeight >= height * 0.2 || textWidth > width * 0.2))
                {
                    size -= 2;
                    font = font.deriveFont(font.getStyle(), size);
                    fm = comp.getFontMetrics(font);
                    textWidth = fm.stringWidth(comp.getText());
                    textHeight = fm.getHeight();
                }

                comp.setFont(font);
            }

            protected void increaseFontSize(JLabel comp)
            {
                Font font = comp.getFont();
                FontMetrics fm = comp.getFontMetrics(font);
                int width = comp.getWidth();
                int height = comp.getHeight();
                int textWidth = fm.stringWidth(comp.getText());
                int textHeight = fm.getHeight();

                int size = font.getSize();
                while(textHeight <= height * 0.2 || textWidth <= width * 0.2)
                {
                    size += 2;
                    font = font.deriveFont(font.getStyle(), size);
                    fm = comp.getFontMetrics(font);
                    textWidth = fm.stringWidth(comp.getText());
                    textHeight = fm.getHeight();
                }

                comp.setFont(font);
                decreaseFontSize(comp);
            }

            @Override
            public void componentResized(ComponentEvent e)
            {
                JLabel comp = (JLabel) e.getComponent();
                Font font = comp.getFont();
                FontMetrics fm = comp.getFontMetrics(font);
                int width = comp.getWidth();
                int height = comp.getHeight();
                int textWidth = fm.stringWidth(comp.getText());
                int textHeight = fm.getHeight();

                if(textHeight > height || textWidth > width)
                    decreaseFontSize(comp);
                else
                    increaseFontSize(comp);
            }
        });
        blockPanel.add(pause, BorderLayout.CENTER);

        return blockPanel;
    }

    private void turnOnTimer()
    {
        start(Minesweeper::repaintControllersTimer);
    }

    public void repaintTimer()
    {
        timerCountL.setText(getTimeString(getSeconds() / 3600) + ":" + getTimeString((getSeconds() % 3600) / 60) + ":" + getTimeString(getSeconds() % 60));
    }

    public void setNumCell(int x, int y, int num)
    {
        CustomButton btn = cells[y * Minesweeper.getFieldSize().width + x];
        Color col = switch(num)
                {
                    case 1 -> new Color(129, 255, 135, 255);
                    case 2 -> new Color(255, 240, 151, 255);
                    case 3 -> new Color(255, 184, 113, 255);
                    case 4 -> new Color(255, 134, 86, 255);
                    case 5 -> new Color(200, 92, 91, 255);
                    case 6 -> new Color(178, 75, 94, 255);
                    case 7 -> new Color(195, 44, 57, 255);
                    case 8 -> new Color(195, 2, 0, 255);
                    default -> Color.WHITE;
                };
        btn.setBackground(col);
        btn.setText("" + num);
        btn.setIcon(null);
        btn.setDisabledIcon(null);
        btn.repaint();
        btn.setEnabled(false);
    }

    public void markCell(int x, int y)
    {
        CustomButton btn = cells[y * Minesweeper.getFieldSize().width + x];
        btn.setIcon(new ImageIcon(flag.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
        btn.repaint();
        imageMap.put(btn, flag);
    }

    public void markMaybeCell(int x, int y)
    {
        CustomButton btn = cells[y * Minesweeper.getFieldSize().width + x];
        btn.setIcon(new ImageIcon(flagMaybe.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
        btn.repaint();
        imageMap.put(btn, flagMaybe);
    }

    public void offMarkOnCell(int x, int y)
    {
        CustomButton btn = cells[y * Minesweeper.getFieldSize().width + x];
        btn.setIcon(null);
        btn.setDisabledIcon(null);
        btn.repaint();
        imageMap.remove(btn);
    }

    public void freeCell(int x, int y)
    {
        CustomButton btn = cells[y * Minesweeper.getFieldSize().width + x];
        btn.setBackground(Color.WHITE);
        btn.setIcon(null);
        btn.setDisabledIcon(null);
        btn.setEnabled(false);
        btn.repaint();
    }

    public void onPause()
    {
        if(!isRunning())
            return;
        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.gridy = 0;
        c0.fill = GridBagConstraints.BOTH;
        c0.anchor = GridBagConstraints.CENTER;

        remove(minePanel);
        pausePanel = createPausePanel();
        add(pausePanel, c0);
        revalidate();
        repaint();
        pause.setText("Run?");
        stop();
        for(var a : cells)
            a.setEnabled(false);
    }

    public void offPause()
    {
        if(isRunning() || isButtonStop)
            return;

        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.gridy = 0;
        c0.fill = GridBagConstraints.BOTH;
        c0.anchor = GridBagConstraints.CENTER;

        pause.setText("Pause?");
        remove(pausePanel);
        pausePanel = null;
        add(minePanel, c0);
        revalidate();
        repaint();

        on();
        for(var a : cells)
            if(a.getBackground() == UIDesigner.BUTTON_BACKGROUND)
            {
                a.setEnabled(true);
            }
    }

    public void setPause()
    {
        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.gridy = 0;
        c0.fill = GridBagConstraints.BOTH;
        c0.anchor = GridBagConstraints.CENTER;

        if(isRunning())
        {
            remove(minePanel);
            pausePanel = createPausePanel();
            add(pausePanel, c0);
            revalidate();
            repaint();
            pause.setText("Run?");
            isButtonStop = true;
            if(Minesweeper.isMainController(this))
                stop();
            for(var a : cells)
                a.setEnabled(false);
        } else
        {
            if(pausePanel != null)
            {
                remove(pausePanel);
                pausePanel = null;
                add(minePanel, c0);
            }
            pause.setText("Pause?");
            revalidate();
            repaint();

            isButtonStop = false;
            if(Minesweeper.isMainController(this))
                on();
            for(var a : cells)
                if(a.getBackground() == UIDesigner.BUTTON_BACKGROUND && Minesweeper.isMainController(this))
                    a.setEnabled(true);
        }
    }

    public void disableGame(byte[][] map)
    {
        if(Minesweeper.isMainController(this))
            stop();

        for(int x = 0; x < map.length; x++)
            for(int y = 0; y < map[x].length; y++)
            {
                CustomButton btn = cells[y * Minesweeper.getFieldSize().width + x];
                if(Minesweeper.isMaybeMarked(map[x][y]))
                {
                    if(Minesweeper.isMined(map[x][y]))
                    {
                        btn.setIcon(new ImageIcon(goodFlagMaybe.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, goodFlagMaybe);
                    } else
                    {
                        btn.setIcon(new ImageIcon(badFlagMaybe.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, badFlagMaybe);
                    }
                    btn.setDisabledIcon(btn.getIcon());
                } else if(Minesweeper.isMarked(map[x][y]))
                {
                    if(Minesweeper.isMined(map[x][y]))
                    {
                        btn.setIcon(new ImageIcon(goodFlag.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, goodFlag);
                    } else
                    {
                        btn.setIcon(new ImageIcon(badFlag.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, badFlag);
                    }
                    btn.setDisabledIcon(btn.getIcon());
                } else if(Minesweeper.isMined(map[x][y]))
                {
                    btn.setIcon(new ImageIcon(bomb.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                    imageMap.put(btn, bomb);
                }
            }

        for(var a : cells)
            a.setEnabled(false);
        pause.setVisible(false);
    }

    private void setToMinimum()
    {
        parent.setMinimumSize(new Dimension(100, 100));
    }

    private void updatePanel()
    {
        parent.revalidate();
        parent.pack();
        parent.setMinimumSize(parent.getSize());
        parent.setResizable(true);
    }

    private void makeFrameListeners()
    {
        if(Minesweeper.isMainController(this))
        {
            parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            parent.addWindowFocusListener(new WindowFocusListener()
            {
                @Override
                public void windowGainedFocus(WindowEvent e)
                {
                    if(Minesweeper.isGameStarted() && !isButtonStop)
                    {
                        Minesweeper.pauseControllers();
                        offPause();
                    }
                }

                @Override
                public void windowLostFocus(WindowEvent e)
                {
                    if(Minesweeper.isGameStarted() && !isButtonStop)
                    {
                        Minesweeper.pauseControllers();
                        onPause();
                    }
                }
            });

            parent.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    if(Minesweeper.isGameStarted() && Minesweeper.isMainController(((GameViewController) ((JFrame) e.getWindow()).getContentPane())))
                    {
                        int a = JOptionPane.showConfirmDialog(parent,
                                "<html><h2>You are sure?</h2><i>Do you want close the game?</i>",
                                "Message",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if(a != JOptionPane.YES_OPTION)
                            return;
                    }
                    Minesweeper.removeController(((GameViewController) ((JFrame) e.getWindow()).getContentPane()));
                    if(Minesweeper.isMainController(((GameViewController) ((JFrame) e.getWindow()).getContentPane())))
                        System.exit(0);
                }
            });
        }
    }
}