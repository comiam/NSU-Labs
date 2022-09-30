package comiam.chattree.message;

import comiam.chattree.node.NeighbourNode;
import comiam.chattree.json.JSONCore;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Message implements Serializable
{

    private final MessageType header;

    private final String data;
    private final String senderName;
    private final String receiverName;

    private final NeighbourNode replacement;

    private final UUID senderID;
    private UUID ID;

    public Message(MessageType header, String senderName, UUID senderID, NeighbourNode replacement, String data, String receiverName)
    {
        this.header = header;
        this.data = data;
        this.senderName = senderName;
        this.senderID = senderID;
        this.receiverName = receiverName;
        this.replacement = replacement;
        this.ID = UUID.randomUUID();
    }

    public void setID(UUID ID)
    {
        this.ID = ID;
    }

    public UUID getSenderID()
    {
        return senderID;
    }

    public String getMessage()
    {
        return data;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public MessageType getHeader()
    {
        return header;
    }

    public UUID getID()
    {
        return ID;
    }

    public NeighbourNode getReplacement()
    {
        return replacement;
    }

    @Override
    public String toString()
    {
        return ID.toString() + ": (header:" + header + ", from: " + senderName + " to: " + receiverName + "): " + data;
    }

    public static DatagramPacket convertToPacket(Message message, NeighbourNode receiver)
    {
        byte[] buf = JSONCore.saveToJSON(message).trim().getBytes(StandardCharsets.UTF_8);

        return new DatagramPacket(buf, buf.length, receiver.getIp(), receiver.getPort());
    }

}
