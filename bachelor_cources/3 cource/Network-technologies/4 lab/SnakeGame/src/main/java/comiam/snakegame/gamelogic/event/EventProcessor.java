package comiam.snakegame.gamelogic.event;

import java.util.function.Predicate;

public interface EventProcessor
{
    HandlerDescriptor addHandler(
            Predicate<Event> shouldHandle,
            EventHandler handler);

    HandlerDescriptor addOneOffHandler(
            Predicate<Event> shouldHandle,
            EventHandler handler);
}
