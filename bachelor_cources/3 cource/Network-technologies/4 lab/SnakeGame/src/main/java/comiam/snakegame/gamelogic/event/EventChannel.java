package comiam.snakegame.gamelogic.event;

@FunctionalInterface
public interface EventChannel
{
    void submit(Event event) throws InterruptedException;
}
