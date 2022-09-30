package comiam.chat.server.data.units;

public class Message
{
    private final String message;
    private final String date;
    private final String username;

    public Message(String message, String date, String username)
    {
        this.message = message;
        this.date = date;
        this.username = username;
    }

    public String getText()
    {
        return message;
    }

    public String getDate()
    {
        return date;
    }

    public String getUsername()
    {
        return username;
    }
}

