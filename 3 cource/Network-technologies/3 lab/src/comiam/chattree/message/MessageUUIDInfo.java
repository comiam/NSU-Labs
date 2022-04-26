package comiam.chattree.message;

import java.util.Objects;
import java.util.UUID;

public class MessageUUIDInfo
{
    private final UUID msgID;
    private final UUID receiveID;

    public MessageUUIDInfo(UUID msgID, UUID nodeID)
    {
        this.msgID = msgID;
        this.receiveID = nodeID;
    }

    public UUID getReceiveID()
    {
        return receiveID;
    }

    @Override
    public boolean equals(Object o)
    {
        try
        {
            MessageUUIDInfo no = (MessageUUIDInfo) o;
            return Objects.equals(msgID, no.msgID) && Objects.equals(receiveID, no.receiveID);
        } catch (Throwable e)
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(msgID, receiveID);
    }
}
