package comiam.chat.server.messages.types;

import java.util.ArrayList;

public class MessagePackage
{
    private final MessageType type;
    private String data = null;

    public MessagePackage(MessageType type, String data)
    {
        this.type = type;
        this.data = data;
    }

    public MessageType getType()
    {
        return type;
    }

    public String getData()
    {
        return data;
    }
}
