package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.event.Event;

public class IncomingMessage implements Event
{
    public AddressedMessage message;

    public IncomingMessage(AddressedMessage message)
    {
        this.message = message;
    }
}
