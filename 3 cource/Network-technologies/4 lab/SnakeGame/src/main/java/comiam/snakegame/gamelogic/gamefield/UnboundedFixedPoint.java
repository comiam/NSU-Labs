package comiam.snakegame.gamelogic.gamefield;

public class UnboundedFixedPoint implements Coordinates
{
    private final int x;
    private final int y;

    public UnboundedFixedPoint(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public boolean equals(final Object o)
    {
        if (o == this) return true;
        if (!(o instanceof UnboundedFixedPoint)) return false;
        final UnboundedFixedPoint other = (UnboundedFixedPoint) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getX() != other.getX()) return false;
        if (this.getY() != other.getY()) return false;
        return true;
    }

    protected boolean canEqual(final Object other)
    {
        return other instanceof UnboundedFixedPoint;
    }

    public int hashCode()
    {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        return result;
    }

    public String toString()
    {
        return "UnboundedFixedPoint(x=" + this.getX() + ", y=" + this.getY() + ")";
    }
}
