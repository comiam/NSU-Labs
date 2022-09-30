package comiam.sapper.ui.gui.components;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static comiam.sapper.ui.gui.components.UIDesigner.BUTTON_BACKGROUND;
import static comiam.sapper.ui.gui.components.UIDesigner.BUTTON_BACKGROUND_PRESSED;

public class CustomButton extends JButton
{
    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public CustomButton()
    {
        this(null);
    }

    public CustomButton(String text)
    {
        super(text);
        super.setContentAreaFilled(false);
        setBackground(BUTTON_BACKGROUND);
        setPressedBackgroundColor(BUTTON_BACKGROUND_PRESSED);
        setHoverBackgroundColor(BUTTON_BACKGROUND.brighter());
        setForeground(Color.LIGHT_GRAY);
        setFocusPainted(false);
        setBorder(new LineBorder(Color.GRAY, 1));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if(getModel().isPressed())
        {
            g.setColor(pressedBackgroundColor);
        } else if(getModel().isRollover())
        {
            g.setColor(hoverBackgroundColor);
        } else
        {
            g.setColor(getBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    public void setHoverBackgroundColor(Color hoverBackgroundColor)
    {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    public void setPressedBackgroundColor(Color pressedBackgroundColor)
    {
        this.pressedBackgroundColor = pressedBackgroundColor;
    }
}