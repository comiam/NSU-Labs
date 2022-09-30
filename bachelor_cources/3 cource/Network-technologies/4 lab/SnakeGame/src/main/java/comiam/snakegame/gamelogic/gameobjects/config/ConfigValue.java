package comiam.snakegame.gamelogic.gameobjects.config;

public class ConfigValue implements Config
{
    private int planeWidth = DEFAULT_WIDTH;
    private int planeHeight = DEFAULT_HEIGHT;
    private int foodStatic = DEFAULT_FOOD_STATIC;
    private float foodPerPlayer = DEFAULT_PER_PLAYER;
    private float foodSpawnOnDeathChance = DEFAULT_FOOD_SPAWN_ON_DEATH_CHANCE;
    private int stateDelayMs = DEFAULT_STEP_DELAY_MS;
    private int pingDelayMs = DEFAULT_PING_DELAY_MS;
    private int nodeTimeoutMs = DEFAULT_NODE_TIMEOUT_MS;

    public ConfigValue()
    {
    }

    public static ConfigValue copyOf(final Config other)
    {
        var result = new ConfigValue();
        result.setPlaneWidth(other.getPlaneWidth());
        result.setPlaneHeight(other.getPlaneHeight());
        result.setFoodStatic(other.getFoodStatic());
        result.setFoodPerPlayer(other.getFoodPerPlayer());
        result.setFoodSpawnOnDeathChance(other.getFoodSpawnOnDeathChance());
        result.setStateDelayMs(other.getStateDelayMs());
        result.setPingDelayMs(other.getPingDelayMs());
        result.setNodeTimeoutMs(other.getNodeTimeoutMs());
        return result;
    }

    public int getPlaneWidth()
    {
        return this.planeWidth;
    }

    public int getPlaneHeight()
    {
        return this.planeHeight;
    }

    public int getFoodStatic()
    {
        return this.foodStatic;
    }

    public float getFoodPerPlayer()
    {
        return this.foodPerPlayer;
    }

    public float getFoodSpawnOnDeathChance()
    {
        return this.foodSpawnOnDeathChance;
    }

    public int getStateDelayMs()
    {
        return this.stateDelayMs;
    }

    public int getPingDelayMs()
    {
        return this.pingDelayMs;
    }

    public int getNodeTimeoutMs()
    {
        return this.nodeTimeoutMs;
    }

    public void setPlaneWidth(int planeWidth)
    {
        this.planeWidth = planeWidth;
    }

    public void setPlaneHeight(int planeHeight)
    {
        this.planeHeight = planeHeight;
    }

    public void setFoodStatic(int foodStatic)
    {
        this.foodStatic = foodStatic;
    }

    public void setFoodPerPlayer(float foodPerPlayer)
    {
        this.foodPerPlayer = foodPerPlayer;
    }

    public void setFoodSpawnOnDeathChance(float foodSpawnOnDeathChance)
    {
        this.foodSpawnOnDeathChance = foodSpawnOnDeathChance;
    }

    public void setStateDelayMs(int stateDelayMs)
    {
        this.stateDelayMs = stateDelayMs;
    }

    public void setPingDelayMs(int pingDelayMs)
    {
        this.pingDelayMs = pingDelayMs;
    }

    public void setNodeTimeoutMs(int nodeTimeoutMs)
    {
        this.nodeTimeoutMs = nodeTimeoutMs;
    }

    public boolean equals(final Object o)
    {
        if (o == this) return true;
        if (!(o instanceof ConfigValue)) return false;
        final ConfigValue other = (ConfigValue) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPlaneWidth() != other.getPlaneWidth()) return false;
        if (this.getPlaneHeight() != other.getPlaneHeight()) return false;
        if (this.getFoodStatic() != other.getFoodStatic()) return false;
        if (Float.compare(this.getFoodPerPlayer(), other.getFoodPerPlayer()) != 0) return false;
        if (Float.compare(this.getFoodSpawnOnDeathChance(), other.getFoodSpawnOnDeathChance()) != 0) return false;
        if (this.getStateDelayMs() != other.getStateDelayMs()) return false;
        if (this.getPingDelayMs() != other.getPingDelayMs()) return false;
        if (this.getNodeTimeoutMs() != other.getNodeTimeoutMs()) return false;
        return true;
    }

    protected boolean canEqual(final Object other)
    {
        return other instanceof ConfigValue;
    }

    public int hashCode()
    {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPlaneWidth();
        result = result * PRIME + this.getPlaneHeight();
        result = result * PRIME + this.getFoodStatic();
        result = result * PRIME + Float.floatToIntBits(this.getFoodPerPlayer());
        result = result * PRIME + Float.floatToIntBits(this.getFoodSpawnOnDeathChance());
        result = result * PRIME + this.getStateDelayMs();
        result = result * PRIME + this.getPingDelayMs();
        result = result * PRIME + this.getNodeTimeoutMs();
        return result;
    }

    public String toString()
    {
        return "ConfigData(planeWidth=" + this.getPlaneWidth() + ", planeHeight=" + this.getPlaneHeight() + ", foodStatic=" + this.getFoodStatic() + ", foodPerPlayer=" + this.getFoodPerPlayer() + ", foodSpawnOnDeathChance=" + this.getFoodSpawnOnDeathChance() + ", stateDelayMs=" + this.getStateDelayMs() + ", pingDelayMs=" + this.getPingDelayMs() + ", nodeTimeoutMs=" + this.getNodeTimeoutMs() + ")";
    }
}
