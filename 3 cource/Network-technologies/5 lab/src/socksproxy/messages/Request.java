package socksproxy.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Request extends ToolsMessage
{
    public Request(ByteBuffer buffer)
    {
        super(new byte[buffer.limit()]);
        buffer.get(this.data);
        if (!Request.isCorrect(this.data))
            throw new IllegalArgumentException();
    }

    public boolean isCommand(byte command)
    {
        return command == data[1];
    }

    public byte getAddressType()
    {
        return data[3];
    }

    public byte[] getDestAddress()
    {
        switch (this.getAddressType())
        {
            case IPv4:
                return Arrays.copyOfRange(data, 4, 8);
            case DOMAIN_NAME:
            {
                int length = data[4];
                return Arrays.copyOfRange(data, 5, 5 + length);
            }
            case IPv6:
                return Arrays.copyOfRange(data, 4, 20);
        }
        return null;
    }

    public short getDestPort()
    {
        switch (data[3])
        {
            case IPv4:
                return ByteBuffer.wrap(data, 8, 2).getShort();
            case DOMAIN_NAME:
            {
                int length = data[4];
                return ByteBuffer.wrap(data, 5 + length, 2).getShort();
            }
            case IPv6:
                return ByteBuffer.wrap(data, 20, 2).getShort();
        }
        return -1;
    }

    public byte[] getBytes()
    {
        return data;
    }

    private static boolean isCorrect(byte[] data)
    {
        if (data.length < 5)
            return false;

        if (data[2] != 0x00)
            return false;

        switch (data[3])
        {
            case IPv4:
                if (data.length != 10)
                    return false;
                break;
            case IPv6:
                if (data.length != 22)
                    return false;
                break;
            case DOMAIN_NAME:
                if (data.length != 7 + data[4])
                    return false;
        }
        return true;
    }

    public static boolean isCorrectSizeOfMessage(ByteBuffer data)
    {
        if (data.position() < 5)
            return false;
        switch (data.get(3))
        {
            case IPv4:
                if (data.position() != 10)
                    return false;
                break;
            case IPv6:
                if (data.position() != 22)
                    return false;
                break;
            case DOMAIN_NAME:
                if (data.position() != 7 + data.get(4))
                    return false;
        }
        return true;
    }


}
