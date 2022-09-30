package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.event.Event;

public final class OutgoingMessage implements Event
{
    public AddressedMessage message;

    public OutgoingMessage(AddressedMessage message)
    {
        this.message = message;
    }
}
