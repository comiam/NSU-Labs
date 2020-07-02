package comiam.chat.client.connection.message;

public class MessagePackage
{
    private final MessageType type;
    private final String data;

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
