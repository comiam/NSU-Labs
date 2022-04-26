package comiam.chattree.node.threads;

import java.util.UUID;

import static comiam.chattree.node.DataNode.*;
import static java.lang.Thread.sleep;

public class ConnectionChecker implements Runnable
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
                    sleep(150);
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            long now = System.currentTimeMillis();
            synchronized (getNeighbours())
            {
                UUID deleteNode = null;
                for(var pair : getNeighbours().entrySet())
                {
                    if (pair.getValue().getLastPingTime() != -1 && now - pair.getValue().getLastPingTime() >= DISCONNECT_TIMER)
                    {
                        disconnectNeighbour(pair.getValue());
                        deleteNode = pair.getKey();
                        break;
                    }
                }
                if(deleteNode != null)
                    getNeighbours().remove(deleteNode);
            }
        }
    }
}
