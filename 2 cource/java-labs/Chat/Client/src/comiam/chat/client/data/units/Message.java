package comiam.chat.client.data.units;

public class Message
{
    private final String message;
    private final String date;
    private final User user;

    public Message(String message, String date, User user)
    {
        this.message = message;
        this.date = date;
        this.user = user;
    }

    public String getText()
    {
        return message;
    }

    public String getDate()
    {
        return date;
    }

    public User getUser()
    {
        return user;
    }
}

