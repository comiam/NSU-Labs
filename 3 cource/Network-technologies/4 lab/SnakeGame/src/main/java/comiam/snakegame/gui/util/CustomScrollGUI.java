package comiam.snakegame.gui.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public final class CustomScrollGUI extends BasicScrollBarUI
{

    @Override
    protected void configureScrollBarColors()
    {
        this.thumbColor = Colours.SCROLL_THUMB;
    }

    @Override
    protected JButton createDecreaseButton(final int orientation)
    {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(final int orientation)
    {
        return createZeroButton();
    }

    private static JButton createZeroButton()
    {
        JButton jbutton = new JButton();
        jbutton.setPreferredSize(new Dimension(0, 0));
        jbutton.setMinimumSize(new Dimension(0, 0));
        jbutton.setMaximumSize(new Dimension(0, 0));
        return jbutton;
    }

}
