package comiam.chattree.node;

import comiam.chattree.message.Message;
import comiam.chattree.message.MessageType;
import comiam.chattree.node.threads.*;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static comiam.chattree.node.DataNode.*;

public class ChatNode
{
    private static int percentOfLost;
    private static DatagramSocket socket;

    public static void start(String name, int percentOfLost, int port, String neighbourIP, Integer neighbourPort)
    {
        setName(name);
        ChatNode.percentOfLost = percentOfLost;

        try
        {
            socket = new DatagramSocket(port);
        } catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        if(neighbourIP != null && neighbourPort != port)
            try
            {
                NeighbourNode node = new NeighbourNode(InetAddress.getByName(neighbourIP), neighbourPort);
                tryConnectToNode(getID().toString(), node);
            } catch (UnknownHostException e)
            {
                try {socket.close(); socket = null;}catch (Throwable ignored){}
                e.printStackTrace();
                return;
            }
        runThreads();
    }

    public static void runThreads()
    {
        Thread receiver = new Thread(new MessageReceiver(socket, percentOfLost));
        Thread sender = new Thread(new MessageSender(socket));
        Thread answerSender = new Thread(new AnswerSender(socket));
        Thread msgCleaner = new Thread(new MessageCleaner());
        Thread checker = new Thread(new ConnectionChecker());
        Thread hb = new Thread(new NodeHeartbeat());

        receiver.start();
        sender.start();
        answerSender.start();
        msgCleaner.start();
        checker.start();
        hb.start();

        Scanner scanner = new Scanner(System.in);
        String str;
        while (true)
        {
            str = scanner.nextLine();

            if (str.trim().equals("exit"))
            {
                receiver.interrupt();
                sender.interrupt();
                answerSender.interrupt();
                msgCleaner.interrupt();
                checker.interrupt();
                hb.interrupt();
                socket.close();

                clearNeighbours();

                System.out.println("-- Node " + getName() + " shut down...");

                System.exit(0);
            }

            broadcastMsg(str);
        }
    }

    private static void broadcastMsg(String data)
    {
        synchronized (getNeighbours())
        {
            getNeighbours().values().forEach(node -> {
                Message message = new Message(MessageType.TEXT_MESSAGE, getName(), getID(), null, data, node.getName());
                addMsgToSendList(message, node);
            });
        }

    }
}
