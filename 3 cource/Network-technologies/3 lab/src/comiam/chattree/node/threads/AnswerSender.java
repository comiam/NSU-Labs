package comiam.chattree.node.threads;

import comiam.chattree.message.MessageTransferInfo;

import java.net.DatagramSocket;

import static comiam.chattree.node.DataNode.getCurrentAnswers;
import static java.lang.Thread.sleep;

public class AnswerSender implements Runnable
{
    private final DatagramSocket socket;

    public AnswerSender(DatagramSocket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            synchronized (this)
            {
                try
                {
                    sleep(20);
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            MessageTransferInfo record;

            while ((record = getCurrentAnswers().poll()) != null)
                MessageSender.sendMessageToNode(socket, record.getPacket());
        }
    }
}