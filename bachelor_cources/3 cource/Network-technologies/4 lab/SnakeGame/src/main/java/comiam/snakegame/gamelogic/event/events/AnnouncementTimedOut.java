package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.gamelogic.event.Event;

import java.net.InetSocketAddress;

public class AnnouncementTimedOut implements Event
{

    public InetSocketAddress fromAddress;

    public AnnouncementTimedOut(InetSocketAddress fromAddress)
    {
        this.fromAddress = fromAddress;
    }
}
