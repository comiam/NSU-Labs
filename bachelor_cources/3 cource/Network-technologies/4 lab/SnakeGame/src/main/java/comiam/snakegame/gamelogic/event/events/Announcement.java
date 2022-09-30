package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.event.Event;

public class Announcement implements Event
{
    public final AddressedMessage message;

    public Announcement(AddressedMessage message)
    {
        this.message = message;
    }
}
