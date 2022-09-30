package comiam.sapper.ui.gui.components;

import javax.swing.*;
import java.awt.*;

public class CustomPanel extends JPanel
{
    private boolean getFirstPreferredSize = false;

    @Override
    public Dimension getPreferredSize()
    {
        if(!getFirstPreferredSize)
        {
            getFirstPreferredSize = true;
            return super.getPreferredSize();
        }
        Container c = this.getParent();
        if(c == null)
            return super.getPreferredSize();

        int size = Math.min(c.getHeight(), c.getWidth());
        return new Dimension(size, size);
    }
}