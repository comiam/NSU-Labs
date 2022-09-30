package comiam.snakegame.gamelogic.gamefield;

import org.jetbrains.annotations.Contract;

public interface BoundedPoint extends Coordinates
{
    Coordinates getBounds();

    BoundedPoint copy();

    @Contract(mutates = "this")
    void centerRelativeTo(final BoundedPoint other);

}
