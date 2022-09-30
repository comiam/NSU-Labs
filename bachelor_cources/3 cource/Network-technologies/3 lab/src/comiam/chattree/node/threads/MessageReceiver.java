package comiam.chattree.node.threads;

import comiam.chattree.message.Message;
import comiam.chattree.message.MessageType;
import comiam.chattree.node.NeighbourNode;
import comiam.chattree.message.MessageUUIDInfo;
import comiam.chattree.message.MessageTransferInfo;
import comiam.chattree.json.JSONCore;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static comiam.chattree.node.DataNode.*;

public class MessageReceiver implements Runnable
{
    private final DatagramSocket socket;
    private final int percentOfLost;

    public MessageReceiver(DatagramSocket socket, int percentOfLost)
    {
        this.socket = socket;
        this.percentOfLost = percentOfLost;
    }

    private boolean isPackageLost()
    {
        return ThreadLocalRandom.current().nextInt(0, 100) < percentOfLost;
    }

    @Override
    public void run()
    {
        byte[] buf = new byte[DATAGRAM_BUFFER_SIZE];
        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                DatagramPacket packet = new DatagramPacket(buf, DATAGRAM_BUFFER_SIZE);
                socket.receive(packet);
                if (!isPackageLost())
                    handleReceivedPacket(packet);
            }
        } catch (Throwable ignored){}
    }

    private void handleReceivedPacket(DatagramPacket packet)
    {
        try
        {
            Message message = JSONCore.parseFromJSON(new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim(), Message.class);

            if(message == null)
            {
                System.err.println("Was exception on receiving packet from " + packet.getAddress() + ". Aborting...");
                return;
            }

            synchronized (getNeighbours())
            {
                if (!getNeighbours().containsKey(message.getSenderID())
                        && !message.getHeader().equals(MessageType.SUCCESS_CONNECT_MESSAGE)
                        && !message.getHeader().equals(MessageType.CONNECT_MESSAGE))
                    return;

                NeighbourNode sender = getNeighbours().get(message.getSenderID());
                if (sender == null)
                    sender = new NeighbourNode(packet.getAddress(), packet.getPort());
                sender.setLastHeartbeat(System.currentTimeMillis());

                switch (message.getHeader())
                {
                    case CONNECT_MESSAGE:
                        sender.setName(message.getSenderName());
                        sender.setId(message.getSenderID());
                        sender.setReplacement(message.getReplacement());

                        getNeighbours().put(sender.getID(), sender);

                        sendAnswer(MessageType.SUCCESS_CONNECT_MESSAGE, getName(), message.getID(), sender);

                        printMsg(message, packet, "-- Wow, we have new node:" + message.getSenderName() + "!");

                        if (getReplacement() != null)
                            noticeAboutOurReplacement(sender);

                        break;
                    case SUCCESS_CONNECT_MESSAGE:
                        removeMsgFromSendList(message, sender);

                        sender.setName(message.getSenderName());
                        sender.setId(message.getSenderID());

                        printMsg(message, packet, "-- Connection with node " + message.getSenderName() + " was successful!");

                        getNeighbours().put(message.getSenderID(), sender);
                        break;
                    case TEXT_MESSAGE:
                        printMsg(message, packet, "Node " + message.getSenderName() + ": " + message.getMessage());

                        sendAnswer(MessageType.SUCCESS_RECEIVED_MESSAGE, message.getID().toString(), message.getID(), sender);

                        broadcastMessage(message, sender);
                        break;
                    case SUCCESS_RECEIVED_MESSAGE:
                        sender.setSuccessfulSent(new MessageUUIDInfo(message.getID(), sender.getID()), System.currentTimeMillis());
                        removeMsgFromSendList(message, sender);
                        break;
                    case NEW_REPLACEMENT_MESSAGE:
                        if (message.getReplacement() != null)
                        {
                            sender.setReplacement(message.getReplacement());
                            sendAnswer(MessageType.SUCCESS_RECEIVED_MESSAGE, getName(), message.getID(), sender);
                        }
                        break;
                    case HEARTBEAT_MESSAGE:
                        sender.setLastHeartbeat(System.currentTimeMillis());
                        break;
                }

                if (getReplacement() == null || getReplacement().getID() == getID())
                    findNewReplacement();
            }
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void printMsg(Message message, DatagramPacket packet, String text)
    {
        if(!getReceivedMessages().containsKey(new MessageUUIDInfo(message.getID(), message.getSenderID())))
        {
            System.out.println(text);

            synchronized (getReceivedMessages())
            {
                getReceivedMessages().put(new MessageUUIDInfo(message.getID(), message.getSenderID()),
                        new MessageTransferInfo(System.currentTimeMillis(), packet));
            }
        }
    }

    private void sendAnswer(MessageType header, String data, UUID id, NeighbourNode neighbourNode)
    {
        Message message = new Message(header, getName(), getID(), null, data, neighbourNode.getName());
        message.setID(id);
        addAnswerToSendList(message, neighbourNode);
    }

    private void removeMsgFromSendList(Message message, NeighbourNode receiver)
    {
        synchronized (getCurrentMessages())
        {
            getCurrentMessages().remove(new MessageUUIDInfo(message.getID(), receiver.getID()));
        }
    }

    private void noticeAboutOurReplacement(NeighbourNode receiver)
    {
        Message message = new Message(MessageType.NEW_REPLACEMENT_MESSAGE, getName(), getID(), getReplacement(), "", receiver.getName());
        message.setID(getReplacement().getID());
        addMsgToSendList(message, receiver);
    }

    private void broadcastMessage(Message message, NeighbourNode neighbourNode)
    {
        getNeighbours().values().forEach(node -> {
            if (!node.getID().equals(neighbourNode.getID()))
            {
                Message newMsg = new Message(message.getHeader(),
                        message.getSenderName(), getID(), null, message.getMessage(), node.getName());
                newMsg.setID(message.getID());
                addMsgToSendList(newMsg, node);
            }
        });
    }
}
