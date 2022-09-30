package comiam.chattree.node;

import comiam.chattree.message.Message;
import comiam.chattree.message.MessageType;
import comiam.chattree.message.MessageUUIDInfo;
import comiam.chattree.message.MessageTransferInfo;

import java.net.DatagramPacket;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataNode
{
    public  static final int    DISCONNECT_TIMER     = 10000;
    public  static final int    DATAGRAM_BUFFER_SIZE = 128 * 1024;
    private static final UUID   id                   = UUID.randomUUID();
    private static       String name;

    private static final Map<MessageUUIDInfo, MessageTransferInfo> receivedMessages = new ConcurrentHashMap<>();
    private static final Map<MessageUUIDInfo, MessageTransferInfo> currentMessages  = new ConcurrentHashMap<>();
    private static final Queue<MessageTransferInfo>                currentAnswers   = new ConcurrentLinkedQueue<>();

    private static final Map<UUID, NeighbourNode> neighbours  = new ConcurrentHashMap<>();
    private static       NeighbourNode            replacement = null;

    public static Map<UUID, NeighbourNode> getNeighbours()
    {
        return neighbours;
    }

    public static Map<MessageUUIDInfo, MessageTransferInfo> getCurrentMessages()
    {
        return currentMessages;
    }

    public static Map<MessageUUIDInfo, MessageTransferInfo> getReceivedMessages()
    {
        return receivedMessages;
    }

    public static Queue<MessageTransferInfo> getCurrentAnswers()
    {
        return currentAnswers;
    }

    public static NeighbourNode getReplacement()
    {
        return replacement;
    }

    public static void setReplacement(NeighbourNode replacement)
    {
        DataNode.replacement = replacement;
    }

    public static String getName()
    {
        return name;
    }

    public static void setName(String name)
    {
        DataNode.name = name;
    }

    public static UUID getID()
    {
        return id;
    }

    public static void findNewReplacement()
    {
        if(getNeighbours().values().iterator().hasNext())
        {
            NeighbourNode first = getNeighbours().values().iterator().next();
            setReplacement(first);
            noticeNewReplacement(first);
        }
    }

    public static void addAnswerToSendList(Message message, NeighbourNode receiver)
    {
        DatagramPacket packet = Message.convertToPacket(message, receiver);
        getCurrentAnswers().offer(new MessageTransferInfo(packet));
    }

    public static void addMsgToSendList(Message message, NeighbourNode receiver)
    {
        synchronized (getCurrentMessages())
        {
            if (getCurrentMessages().containsKey(new MessageUUIDInfo(message.getID(), receiver.getID())))
                return;

            MessageUUIDInfo MessageUUIDInfo = new MessageUUIDInfo(message.getID(), receiver.getID());
            if (receiver.isSuccessfullySent(MessageUUIDInfo))
                return;

            DatagramPacket packet = Message.convertToPacket(message, receiver);
            getCurrentMessages().put(MessageUUIDInfo, new MessageTransferInfo(System.currentTimeMillis(), packet));
        }
    }

    public static void noticeNewReplacement(NeighbourNode newReplacement)
    {
        synchronized (getNeighbours())
        {
            getNeighbours().values().forEach(node -> {
                Message message = new Message(MessageType.NEW_REPLACEMENT_MESSAGE, getName(), getID(), newReplacement, "", node.getName());
                addMsgToSendList(message, node);
            });
        }
    }

    public static void disconnectNeighbour(NeighbourNode neighbourNode)
    {
        synchronized (getNeighbours())
        {
            if (neighbourNode == null || !getNeighbours().containsKey(neighbourNode.getID()) || neighbourNode.isDestructed())
                return;

            System.out.println("Can't take heartbeat by node " + neighbourNode.getName() + ". Disconnected...");

            NeighbourNode newReplacement = neighbourNode.getReplacement();

            if (neighbourNode == getReplacement())
            {
                System.out.println("-- oooops, we lost our replacement...\n-- try to find new...");
                setReplacement(null);

                if (newReplacement != null && !getID().equals(newReplacement.getID()))
                {
                    getNeighbours().put(newReplacement.getID(), newReplacement);
                    tryConnectToNode(getID().toString(), newReplacement);
                }else
                    findNewReplacement();
            }

            neighbourNode.interruptCleaner();
            System.gc();
        }
    }

    public static void tryConnectToNode(String data, NeighbourNode node)
    {
        Message message = new Message(MessageType.CONNECT_MESSAGE, getName(), getID(), getReplacement(), data, node.getName());
        addMsgToSendList(message, node);
    }

    public static void clearNeighbours()
    {
        synchronized (getNeighbours())
        {
            getNeighbours().values().forEach(NeighbourNode::interruptCleaner);
            neighbours.clear();
        }
    }
}
