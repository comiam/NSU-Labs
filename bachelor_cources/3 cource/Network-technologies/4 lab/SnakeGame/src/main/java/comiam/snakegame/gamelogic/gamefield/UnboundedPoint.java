package comiam.snakegame.gamelogic.gamefield;

public class UnboundedPoint
{
    public int x;
    public int y;

    public Coordinates toCoordinates()
    {
        return new UnboundedFixedPoint(this.x, this.y);
    }

    public String toString()
    {
        return "UnboundedPoint(x=" + this.x + ", y=" + this.y + ")";
    }
}
