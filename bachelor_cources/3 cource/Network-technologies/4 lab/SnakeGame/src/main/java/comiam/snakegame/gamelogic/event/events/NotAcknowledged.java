package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.event.Event;

public class NotAcknowledged implements Event
{
    public AddressedMessage message;

    public NotAcknowledged(AddressedMessage message)
    {
        this.message = message;
    }
}
