package comiam.chattree.node.threads;

import comiam.chattree.node.DataNode;
import comiam.chattree.message.MessageUUIDInfo;
import comiam.chattree.message.MessageTransferInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

import static comiam.chattree.node.DataNode.*;
import static java.lang.Thread.sleep;

public class MessageSender implements Runnable
{
    private final DatagramSocket socket;

    public MessageSender(DatagramSocket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        long timeout = 70;
        while (!Thread.currentThread().isInterrupted())
        {
            synchronized (this)
            {
                try
                {
                    sleep(25);
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            long now = System.currentTimeMillis();
            synchronized (getCurrentMessages())
            {
                for (Map.Entry<MessageUUIDInfo, MessageTransferInfo> mes : getCurrentMessages().entrySet())
                {
                    DatagramPacket packet = mes.getValue().getPacket();
                    long lastTimeStamp = mes.getValue().getLastTimeStamp();
                    long firstTimeStamp = mes.getValue().getFirstTimeStamp();

                    if (now - lastTimeStamp > timeout || lastTimeStamp == firstTimeStamp)
                    {
                        sendMessageToNode(socket, packet);
                        mes.getValue().setLastTimeStamp(now);
                    }
                }
                getCurrentMessages().entrySet().removeIf(e -> System.currentTimeMillis() - e.getValue().getFirstTimeStamp() >= DISCONNECT_TIMER);
            }
        }
    }

    public static synchronized void sendMessageToNode(DatagramSocket socket, DatagramPacket packet)
    {
        try
        {
            socket.send(packet);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
