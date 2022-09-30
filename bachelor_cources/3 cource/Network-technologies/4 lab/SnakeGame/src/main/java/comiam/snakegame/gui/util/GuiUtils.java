package comiam.snakegame.gui.util;

import comiam.snakegame.util.Constants;

import javax.swing.*;
import java.awt.*;

public final class GuiUtils
{
    private GuiUtils()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void setUIComponentsColors()
    {
        UIManager.put("Label.foreground", Colours.TEXT);
        UIManager.put("Label.background", Colours.INTERFACE_BACKGROUND);
        UIManager.put("Label.border", BorderFactory.createEmptyBorder());

        UIManager.put("TabbedPane.contentAreaColor", Colours.INTERFACE_BACKGROUND);
        UIManager.put("TabbedPane.selected", Colours.INTERFACE_BACKGROUND);
        UIManager.put("TabbedPane.unselected", Colours.DARK_LINING);
        UIManager.put("TabbedPane.background", Colours.DARK_LINING);
        UIManager.put("TabbedPane.foreground", Colours.TEXT);
        UIManager.put("TabbedPane.shadow", Colours.INTERFACE_BACKGROUND);
        UIManager.put("TabbedPane.borderHightlightColor", Colours.LINING);
        UIManager.put("TabbedPane.borderColor", Colours.LINING);
        UIManager.put("TabbedPane.light", Colours.LINING);
        UIManager.put("TabbedPane.lightHighlight", Colours.LINING);
        UIManager.put("TabbedPane.darkShadow", Colours.LINING);
        UIManager.put("TabbedPane.focus", Colours.INTERFACE_BACKGROUND);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 1, 0, 0));
        UIManager.put("TabbedPane.border", BorderFactory.createLineBorder(Colours.LINING, 1, true));

        UIManager.put("OptionPane.background", Colours.INTERFACE_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", Colours.TEXT);

        UIManager.put("Panel.background", Colours.INTERFACE_BACKGROUND);

        UIManager.put("Button.foreground", Colours.TEXT);
        UIManager.put("Button.background", Colours.INTERFACE_BACKGROUND);
        UIManager.put("Button.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 2, 2, 2, Colours.INTERFACE_BACKGROUND),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Colours.LIGHT_LINING, 1, true),
                                BorderFactory.createEmptyBorder(4, 10, 4, 10))));
        UIManager.put("Button.background", Colours.INTERFACE_BACKGROUND);

        UIManager.put("TextField.foreground", Colours.TEXT);
        UIManager.put("TextField.background", Colours.TEXT_ENTRY_FORM);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 2, 4, 2, Colours.INTERFACE_BACKGROUND),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Colours.LIGHT_LINING, 1, false),
                        BorderFactory.createEmptyBorder(4, 3, 4, 3))));

        UIManager.put("Slider.foreground", Colours.TEXT);
        UIManager.put("Slider.background", Colours.TEXT_ENTRY_FORM);

        UIManager.put("ToolTip.foreground", Colours.TEXT);
        UIManager.put("ToolTip.background", Colours.TOOLTIP);
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colours.LIGHT_LINING, 1, false),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    }

    public static void setColours(
            final JComponent component,
            final Color foreground,
            final Color background)
    {
        component.setForeground(foreground);
        component.setBackground(background);
    }

    public static String trimNameToFitMaxLength(
            final String name,
            final boolean bot,
            final boolean zombie)
    {
        var result = new StringBuilder(name);
        if (bot)
        {
            result.insert(0, "[B] ");
        }
        if (zombie)
        {
            result.insert(0, "[Z] ");
        }
        var newName = result.toString();
        if (newName.length() <= Constants.MAX_NAME_LENGTH)
        {
            return newName;
        }
        return newName.substring(0, Constants.MAX_NAME_LENGTH - 3).trim() + "...";
    }
}
