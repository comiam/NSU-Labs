package comiam.chattree.message;

import java.net.DatagramPacket;

public class MessageTransferInfo
{
    private Long firstTimeStamp;
    private Long lastTimeStamp;
    private final DatagramPacket sendData;

    public MessageTransferInfo(Long firstTimeStamp, DatagramPacket sendData)
    {
        this.firstTimeStamp = firstTimeStamp;
        this.lastTimeStamp = firstTimeStamp;
        this.sendData = sendData;
    }

    public MessageTransferInfo(DatagramPacket sendData)
    {
        this.sendData = sendData;
    }

    public void setLastTimeStamp(Long lastTimeStamp)
    {
        this.lastTimeStamp = lastTimeStamp;
    }

    public Long getFirstTimeStamp()
    {
        return firstTimeStamp;
    }

    public Long getLastTimeStamp()
    {
        return lastTimeStamp;
    }

    public DatagramPacket getPacket()
    {
        return sendData;
    }
}
