package comiam.snakegame.gamelogic.gamefield;

import java.util.function.Consumer;

public class BoundedMovablePoint implements BoundedPoint
{
    private final int boundX;
    private final int boundY;

    private int x;
    private int y;

    public BoundedMovablePoint(final int x, final int y, final  Coordinates bounds)
    {
        if (bounds.getX() <= 0 || bounds.getY() <= 0)
        {
            throw new IllegalArgumentException("Invalid bounds");
        }
        this.boundX = bounds.getX();
        this.boundY = bounds.getY();

        this.x = x;
        this.y = y;

        this.makeFitBounds();
    }

    public BoundedMovablePoint(final  Coordinates coordinates, final  Coordinates bounds)
    {
        if (bounds.getX() <= 0 || bounds.getY() <= 0)
        {
            throw new IllegalArgumentException("Invalid bounds");
        }
        this.boundX = bounds.getX();
        this.boundY = bounds.getY();

        this.x = coordinates.getX();
        this.y = coordinates.getY();

        this.makeFitBounds();
    }

    public BoundedMovablePoint(final  BoundedPoint other)
    {
        if (other.getBounds().getX() <= 0 || other.getBounds().getY() <= 0)
        {
            throw new IllegalArgumentException("Invalid bounds");
        }

        this.boundX = other.getBounds().getX();
        this.boundY = other.getBounds().getY();
        this.x = other.getX();
        this.y = other.getY();

        this.makeFitBounds();
    }

    public BoundedMovablePoint(final int x, final int y, final int boundX, final int boundY)
    {
        if (boundX <= 0 || boundY <= 0)
        {
            throw new IllegalArgumentException("Invalid bounds");
        }
        this.boundX = boundX;
        this.boundY = boundY;

        this.x = x;
        this.y = y;

        this.makeFitBounds();
    }

    @Override
    public  Coordinates getBounds()
    {
        return new UnboundedFixedPoint(this.boundX, this.boundY);
    }

    @Override
    public  BoundedMovablePoint copy()
    {
        return new BoundedMovablePoint(this.getX(), this.getY(), this.boundX, this.boundY);
    }

    public void setX(final int x)
    {
        this.x = makeFit(x, this.boundX);
    }

    public void setY(final int y)
    {
        this.x = makeFit(y, this.boundY);
    }

    public void setXY(final int x, final int y)
    {
        this.x = x;
        this.y = y;

        this.makeFitBounds();
    }

    public void setCoordinates(final  Coordinates coordinates)
    {
        this.x = coordinates.getX();
        this.y = coordinates.getY();

        this.makeFitBounds();
    }

    private void makeFitBounds()
    {
        this.x = makeFit(this.x, this.boundX);
        this.y = makeFit(this.y, this.boundY);
    }

    private static int makeFit(final int value, final int max)
    {
        int i = value;
        while (i < 0)
        {
            i += max;
        }
        return i % max;
    }

    public void move(final  Coordinates offset)
    {
        this.x += offset.getX();
        this.y += offset.getY();
        this.makeFitBounds();
    }

    public void move(final int offsetX, final int offsetY)
    {
        this.x += offsetX;
        this.y += offsetY;
        this.makeFitBounds();
    }

    public void move(final  Direction direction, final int times)
    {
        var dx = 0;
        var dy = 0;
        switch (direction)
        {
            case UP -> dy = -times;
            case DOWN -> dy = times;
            case LEFT -> dx = -times;
            case RIGHT -> dx = times;
        }
        this.setXY(this.getX() + dx, this.getY() + dy);
    }

    public  BoundedMovablePoint moved(final  Direction direction)
    {
        var dx = 0;
        var dy = 0;
        switch (direction)
        {
            case UP -> dy = -1;
            case DOWN -> dy = 1;
            case LEFT -> dx = -1;
            case RIGHT -> dx = 1;
        }
        return new BoundedMovablePoint(
                this.getX() + dx, this.getY() + dy, this.boundX, this.boundY);
    }

    public void forEachWithinExceptSelf(
            final  Coordinates offset,
            final  Consumer< BoundedPoint> action)
    {
        var dx = offset.getX() < 0 ? -1 : (offset.getX() > 0 ? 1 : 0);
        var dy = offset.getY() < 0 ? -1 : (offset.getY() > 0 ? 1 : 0);

        var point = this.copy();

        if (dx == 0)
        {
            for (int y = this.getY() + dy; y != this.getY() + offset.getY() + dy; y += dy)
            {
                point.setXY(this.getX(), y);
                action.accept(point);
            }
        } else if (dy == 0)
        {
            for (int x = this.getX() + dx; x != this.getX() + offset.getX() + dx; x += dx)
            {
                point.setXY(x, this.getY());
                action.accept(point);
            }
        } else
        {
            for (int x = this.getX() + dx; x != this.getX() + offset.getX() + dx; x += dx)
            {
                for (int y = this.getY() + dy; y != this.getY() + offset.getY() + dy; y += dy)
                {
                    point.setXY(x, y);
                    action.accept(point);
                }
            }
        }
    }

    @Override
    public void centerRelativeTo(final  BoundedPoint other)
    {
        if (this.boundX != other.getBounds().getX() || this.boundY != other.getBounds().getY())
        {
            throw new IllegalArgumentException("Different bounds");
        }
        var offsetX = other.getX() - this.boundX / 2;
        var offsetY = other.getY() - this.boundY / 2;

        this.move(-offsetX, -offsetY);
    }

    public boolean equals(final Object o)
    {
        if (o == this) return true;
        if (!(o instanceof BoundedMovablePoint)) return false;
        final BoundedMovablePoint other = (BoundedMovablePoint) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.boundX != other.boundX) return false;
        if (this.boundY != other.boundY) return false;
        if (this.x != other.x) return false;
        if (this.y != other.y) return false;
        return true;
    }

    protected boolean canEqual(final Object other)
    {
        return other instanceof BoundedMovablePoint;
    }

    public int hashCode()
    {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.boundX;
        result = result * PRIME + this.boundY;
        result = result * PRIME + this.x;
        result = result * PRIME + this.y;
        return result;
    }

    public String toString()
    {
        return "BoundedMovablePoint(boundX=" + this.boundX + ", boundY=" + this.boundY + ", x=" + this.x + ", y=" + this.y + ")";
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }
}
