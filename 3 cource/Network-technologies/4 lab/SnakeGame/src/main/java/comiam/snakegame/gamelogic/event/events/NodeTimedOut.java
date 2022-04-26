package comiam.snakegame.gamelogic.event.events;

import comiam.snakegame.gamelogic.event.Event;

import java.net.InetSocketAddress;

public class NodeTimedOut implements Event
{

    public InetSocketAddress nodeAddress;

    public NodeTimedOut(InetSocketAddress nodeAddress)
    {
        this.nodeAddress = nodeAddress;
    }
}
