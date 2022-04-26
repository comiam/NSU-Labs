package comiam.chattree.node;

import comiam.chattree.message.MessageUUIDInfo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static comiam.chattree.node.DataNode.DISCONNECT_TIMER;

public class NeighbourNode implements Serializable
{
    private UUID id;
    private final int port;
    private final InetAddress ip;
    private String name;
    private transient long lastPingTime = -1;
    private final transient Map<MessageUUIDInfo, Long> successfulSentMessages;
    private NeighbourNode replacement;

    private transient Thread cleaner;

    public NeighbourNode(InetAddress ip, int port)
    {
        successfulSentMessages = new HashMap<>();
        this.ip = ip;
        this.port = port;
        this.name = "null";
        this.id = null;
        startCleaner();
    }

    public boolean isDestructed()
    {
        return successfulSentMessages == null;
    }

    public long getLastPingTime()
    {
        return lastPingTime;
    }

    public void setLastHeartbeat(long time)
    {
        lastPingTime = time;
    }

    public NeighbourNode getReplacement()
    {
        return replacement;
    }

    public void setReplacement(NeighbourNode delegate)
    {
        this.replacement = delegate;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getID()
    {
        return id;
    }

    public synchronized void setSuccessfulSent(MessageUUIDInfo MessageUUIDInfo, Long successTime)
    {
        if(isDestructed())
            return;
        successfulSentMessages.put(MessageUUIDInfo, successTime);
    }

    public synchronized boolean isSuccessfullySent(MessageUUIDInfo MessageUUIDInfo)
    {
        if(isDestructed())
            return false;

        return successfulSentMessages.containsKey(MessageUUIDInfo);
    }

    public String getName()
    {
        return name;
    }

    public InetAddress getIp()
    {
        return ip;
    }

    public int getPort()
    {
        return port;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void interruptCleaner()
    {
        if(cleaner != null)
            cleaner.interrupt();
    }

    private void startCleaner()
    {
        cleaner = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() || !isDestructed())
            {
                synchronized (this)
                {
                    try
                    {
                        this.wait(DISCONNECT_TIMER);
                    } catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    successfulSentMessages.entrySet().removeIf(e -> System.currentTimeMillis() - e.getValue() > DISCONNECT_TIMER);
                }
            }
        });

        cleaner.start();
    }
}
