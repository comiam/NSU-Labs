package comiam.snakegame.gui.menu;

import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gamelogic.gameobjects.config.ConfigValue;
import comiam.snakegame.gamelogic.gameobjects.config.ConfigValidator;
import comiam.snakegame.gui.util.Colours;
import comiam.snakegame.gui.game.GameWindow;
import comiam.snakegame.util.unsafe.UnsafeFunction;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HostPanel extends JPanel
{

    private static final int TEXT_FIELD_COLUMNS = 5;

    private static final Border NORMAL_TEXT_FIELD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 2, 4, 2, Colours.INTERFACE_BACKGROUND),
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colours.LINING),
                    BorderFactory.createEmptyBorder(2, 3, 2, 3)));

    private static final Border ERROR_TEXT_FIELD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 2, 4, 2, Colours.INTERFACE_BACKGROUND),
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colours.RED),
                    BorderFactory.createEmptyBorder(2, 3, 2, 3)));

    HostPanel(
            final MenuWindow window,
            final Config baseConfig,
            final GameStarter gameStarter)
    {
        super(new BorderLayout());

        val config = ConfigValue.copyOf(baseConfig);

        this.setPreferredSize(new Dimension(MenuWindow.MENU_PANEL_WIDTH, MenuWindow.MENU_PANEL_HEIGHT));

        val controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Colours.DARK_LINING));

        val startButton = new JButton("Start");
        startButton.addActionListener(
                unused -> {
                    startButton.setEnabled(false);
                    val gameView = new GameWindow();
                    gameView.getExitHookRegisterer().accept(() -> {
                        window.setVisible(true);
                        startButton.setEnabled(true);
                    });
                    gameStarter.startGame(window.topPanel.getName(), config, gameView);
                    window.setVisible(false);
                });
        startButton.setFocusPainted(false);
        startButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 2, 2, 2),
                        startButton.getBorder()));
        controlPanel.add(startButton, BorderLayout.EAST);

        this.add(controlPanel, BorderLayout.SOUTH);

        val configPanel = new JPanel(new GridLayout(8, 2));

        val checker = (Supplier<Boolean>) () -> ConfigValidator.isValid(config);
        val onChanged = (Consumer<Boolean>) ok -> {
            startButton.setEnabled(ok);
            if (ok)
            {
                config.store();
            }
        };
        val toIntConverter = (UnsafeFunction<String, Integer>) Integer::valueOf;
        val toFloatConverter = (UnsafeFunction<String, Float>) Float::valueOf;

        val width = createTextArea(
                baseConfig.getPlaneWidth(), toIntConverter, config::setPlaneWidth, checker, onChanged);
        width.setToolTipText(createTooltip(ConfigValidator.MIN_WIDTH, ConfigValidator.MAX_WIDTH));
        val height = createTextArea(
                baseConfig.getPlaneHeight(), toIntConverter, config::setPlaneHeight, checker, onChanged);
        height.setToolTipText(createTooltip(ConfigValidator.MIN_HEIGHT, ConfigValidator.MAX_HEIGHT));
        val foodStatic = createTextArea(
                baseConfig.getFoodStatic(), toIntConverter, config::setFoodStatic, checker, onChanged);
        foodStatic.setToolTipText(createTooltip(ConfigValidator.MIN_FOOD_STATIC, ConfigValidator.MAX_FOOD_STATIC));
        val foodPerPlayer = createTextArea(
                baseConfig.getFoodPerPlayer(), toFloatConverter, config::setFoodPerPlayer, checker, onChanged);
        foodPerPlayer.setToolTipText(
                createTooltip(ConfigValidator.MIN_FOOD_PER_PLAYER, ConfigValidator.MAX_FOOD_PER_PLAYER));
        val foodSpawnOnDeathChance = createTextArea(
                baseConfig.getFoodSpawnOnDeathChance(), toFloatConverter,
                config::setFoodSpawnOnDeathChance, checker, onChanged);
        foodSpawnOnDeathChance.setToolTipText(
                createTooltip(
                        ConfigValidator.MIN_FOOD_SPAWN_ON_DEATH_CHANCE,
                        ConfigValidator.MAX_FOOD_SPAWN_ON_DEATH_CHANCE));
        val stateDelayMs = createTextArea(
                baseConfig.getStateDelayMs(), toIntConverter, config::setStateDelayMs, checker, onChanged);
        stateDelayMs.setToolTipText(
                createTooltip(ConfigValidator.MIN_STEP_DELAY_MS, ConfigValidator.MAX_STEP_DELAY_MS));
        val pingDelayMs = createTextArea(
                baseConfig.getPingDelayMs(), toIntConverter, config::setPingDelayMs, checker, onChanged);
        pingDelayMs.setToolTipText(createTooltip(ConfigValidator.MIN_PING_DELAY_MS, ConfigValidator.MAX_PING_DELAY_MS));
        val nodeTimeoutMs = createTextArea(
                baseConfig.getNodeTimeoutMs(), toIntConverter, config::setNodeTimeoutMs, checker, onChanged);
        nodeTimeoutMs.setToolTipText(
                createTooltip(ConfigValidator.MIN_NODE_TIMEOUT_MS, ConfigValidator.MAX_NODE_TIMEOUT_MS));

        configPanel.add(createLabel("Width:"));
        configPanel.add(createInputPanel(width));
        configPanel.add(createLabel("Height:"));
        configPanel.add(createInputPanel(height));
        configPanel.add(createLabel("Food static:"));
        configPanel.add(createInputPanel(foodStatic));
        configPanel.add(createLabel("Food per player:"));
        configPanel.add(createInputPanel(foodPerPlayer));
        configPanel.add(createLabel("Chance of food replacing dead snake's segment:"));
        configPanel.add(createInputPanel(foodSpawnOnDeathChance));
        configPanel.add(createLabel("State delay (ms):"));
        configPanel.add(createInputPanel(stateDelayMs));
        configPanel.add(createLabel("Ping delay (ms):"));
        configPanel.add(createInputPanel(pingDelayMs));
        configPanel.add(createLabel("Node timeout (ms):"));
        configPanel.add(createInputPanel(nodeTimeoutMs));

        this.add(configPanel, BorderLayout.NORTH);
    }

    private static <T extends Number> JTextField createTextArea(
            final T numericValue,
            final UnsafeFunction<String, T> converter,
            final Consumer<T> numericValueConsumer,
            final Supplier<Boolean> checker,
            final Consumer<Boolean> onChanged)
    {
        val result = new JTextField(String.valueOf(numericValue), TEXT_FIELD_COLUMNS);
        result.setBorder(NORMAL_TEXT_FIELD_BORDER);
        result.getDocument().addDocumentListener(
                new ConfigChangeListener<>(result, converter, numericValueConsumer, checker, onChanged));

        return result;
    }

    private static <T extends Number> String createTooltip(
            final T lowerBound,
            final T upperBound)
    {
        return "From " + lowerBound + " to " + upperBound;
    }

    private static JPanel createInputPanel(final JTextField textField)
    {
        val panel = new JPanel(new BorderLayout());
        panel.add(textField, BorderLayout.WEST);
        return panel;
    }

    private static JLabel createLabel(final String caption)
    {
        val result = new JLabel(caption);
        result.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        return result;
    }

    @RequiredArgsConstructor
    private static final class ConfigChangeListener<T extends Number> implements DocumentListener
    {

        final JTextField textField;
        private final UnsafeFunction<String, T> converter;
        private final Consumer<T> numericValueConsumer;
        private final Supplier<Boolean> checker;
        private final Consumer<Boolean> onChanged;

        void onTextChanged()
        {
            try
            {
                val text = this.textField.getText();
                val value = this.converter.apply(text);
                this.numericValueConsumer.accept(value);
                val ok = this.checker.get();
                this.textField.setBorder(ok ? NORMAL_TEXT_FIELD_BORDER : ERROR_TEXT_FIELD_BORDER);
                this.onChanged.accept(ok);
            } catch (final Exception e)
            {
                this.textField.setBorder(ERROR_TEXT_FIELD_BORDER);
                this.onChanged.accept(false);
            }
        }

        @Override
        public void insertUpdate(final DocumentEvent e)
        {
            this.onTextChanged();
        }

        @Override
        public void removeUpdate(final DocumentEvent e)
        {
            this.onTextChanged();
        }

        @Override
        public void changedUpdate(final DocumentEvent e)
        {
            this.onTextChanged();
        }
    }
}
