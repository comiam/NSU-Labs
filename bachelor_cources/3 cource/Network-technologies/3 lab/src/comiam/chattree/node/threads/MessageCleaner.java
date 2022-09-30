package comiam.chattree.node.threads;

import static comiam.chattree.node.DataNode.DISCONNECT_TIMER;
import static comiam.chattree.node.DataNode.getReceivedMessages;
import static java.lang.Thread.sleep;

public class MessageCleaner implements Runnable
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
                    sleep(DISCONNECT_TIMER);
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
                synchronized (getReceivedMessages())
                {
                    getReceivedMessages().entrySet().removeIf(e -> System.currentTimeMillis() - e.getValue().getFirstTimeStamp() > DISCONNECT_TIMER);
                }
            }
        }
    }
}
