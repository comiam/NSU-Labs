package comiam.chat.server.messages.types;

public class Request
{
    private final RequestType type;
    private String sessionID;

    private String name;
    private String pass;
    private String message;

    public Request(RequestType type, String sessionID)
    {
        this.type = type;
        this.sessionID = sessionID;
    }

    public Request(RequestType type, String name, String pass)
    {
        this.type = type;
        this.name = name;
        this.pass = pass;
    }

    public Request(RequestType type, String name, String message, String sessionID)
    {
        this.type = type;
        this.name = name;
        this.message = message;
        this.sessionID = sessionID;
    }

    public String getMessage()
    {
        return message;
    }

    public RequestType getType()
    {
        return type;
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public String getName()
    {
        return name;
    }

    public String getPass()
    {
        return pass;
    }
}
