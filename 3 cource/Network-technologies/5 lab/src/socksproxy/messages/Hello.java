package socksproxy.messages;

import socksproxy.auth.AuthMode;

import java.nio.ByteBuffer;

public class Hello extends ToolsMessage
{
    public Hello(ByteBuffer buffer)
    {
        super(new byte[buffer.limit()]);
        buffer.get(data);
        if (data[1] + 2 != data.length)
            throw new IllegalArgumentException();
    }

    public boolean hasMethod(AuthMode authMode)
    {
        byte curMethod = getCurrentMethod(authMode);
        for (int i = 0; i < data[1]; ++i)
            if (curMethod == data[i + 2])
                return true;

        return false;

    }

    public static boolean isCorrectSizeOfMessage(ByteBuffer data)
    {
        return data.position() > 1 && data.position() >= 2 + data.get(1);
    }
}
