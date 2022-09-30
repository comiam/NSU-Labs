package comiam.snakegame.gamelogic.gameobjects.config;

@SuppressWarnings("RemoveLiteralUnderscores")
public final class ConfigValidator
{

    public static final int MIN_WIDTH = 10;
    public static final int MAX_WIDTH = 100;

    public static final int MIN_HEIGHT = 10;
    public static final int MAX_HEIGHT = 100;

    public static final int MIN_FOOD_STATIC = 0;
    public static final int MAX_FOOD_STATIC = 100;

    public static final float MIN_FOOD_PER_PLAYER = 0;
    public static final float MAX_FOOD_PER_PLAYER = 100;

    public static final float MIN_FOOD_SPAWN_ON_DEATH_CHANCE = 0;
    public static final float MAX_FOOD_SPAWN_ON_DEATH_CHANCE = 1;

    public static final int MIN_STEP_DELAY_MS = 1;
    public static final int MAX_STEP_DELAY_MS = 10_000;

    public static final int MIN_PING_DELAY_MS = 1;
    public static final int MAX_PING_DELAY_MS = 10_000;

    public static final int MIN_NODE_TIMEOUT_MS = 1;
    public static final int MAX_NODE_TIMEOUT_MS = 10_000;

    private ConfigValidator()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isValid(final Config config)
    {
        try
        {
            checkField(config.getPlaneWidth(), "width", MIN_WIDTH, MAX_WIDTH);
            checkField(config.getPlaneHeight(), "height", MIN_HEIGHT, MAX_HEIGHT);
            checkField(config.getFoodStatic(), "foodStatic", MIN_FOOD_STATIC, MAX_FOOD_STATIC);
            checkField(config.getFoodPerPlayer(), "foodPerPlayer", MIN_FOOD_PER_PLAYER, MAX_FOOD_PER_PLAYER);
            checkField(
                    config.getFoodSpawnOnDeathChance(), "foodSpawnOnDeathChance",
                    MIN_FOOD_SPAWN_ON_DEATH_CHANCE, MAX_FOOD_SPAWN_ON_DEATH_CHANCE);
            checkField(config.getStateDelayMs(), "stepDelayMs", MIN_STEP_DELAY_MS, MAX_STEP_DELAY_MS);
            checkField(config.getPingDelayMs(), "pingDelayMs", MIN_PING_DELAY_MS, MAX_PING_DELAY_MS);
            checkField(config.getNodeTimeoutMs(), "nodeTimeoutMs", MIN_NODE_TIMEOUT_MS, MAX_NODE_TIMEOUT_MS);
        } catch (final InvalidConfigException e)
        {
            return false;
        }
        return true;
    }

    private static void checkField(
            final int field,
            final String name,
            final int min,
            final int max) throws InvalidConfigException
    {
        if (field < min || field > max)
        {
            throw new InvalidConfigException(
                    String.format("Field \"%s\" value %d is out of range [%d, %d]", name, field, min, max));
        }
    }

    private static void checkField(
            final double field,
            final String name,
            final double min,
            final double max) throws InvalidConfigException
    {
        if (field < min || field > max)
        {
            throw new InvalidConfigException(
                    String.format("Field \"%s\" value %f is out of range [%f, %f]", name, field, min, max));
        }
    }
}
