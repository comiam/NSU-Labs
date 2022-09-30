package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.gamelogic.event.Event;

import java.net.InetSocketAddress;

public class PingTimer implements Event
{
    public InetSocketAddress who;

    public PingTimer(InetSocketAddress who)
    {
        this.who = who;
    }
}
