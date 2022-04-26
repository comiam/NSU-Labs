package comiam.chattree.node.threads;

import comiam.chattree.message.Message;
import comiam.chattree.message.MessageType;

import static comiam.chattree.node.DataNode.*;
import static java.lang.Thread.sleep;

public class NodeHeartbeat implements Runnable
{
    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            synchronized (this)
            {
                try
                {
                    sleep(75);
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            synchronized (getNeighbours())
            {
                getNeighbours().forEach((key, neighbour) ->
                        addAnswerToSendList(new Message(MessageType.HEARTBEAT_MESSAGE,
                                getName(), getID(), null, "", neighbour.getName()), neighbour));
            }
        }
    }
}
