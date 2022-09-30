package comiam.chat.client.connection.message;

public enum ErrorType
{
    BAD_MESSAGE_TYPE,
    RELOGIN_ERROR,
    BAD_MESSAGE_DATA,
    USER_ALREADY_EXIST,
    USER_ALREADY_CONNECTED,
    USER_NOT_EXIST,
    CHAT_ALREADY_EXIST,
    CHAT_NOT_EXIST,
    WRONG_PASSWORD,
    USER_ALREADY_EXIST_IN_CHAT,
    USER_NOT_EXIST_IN_CHAT,
    SESSION_NOT_EXIST,
    SESSION_HAVE_ACTIVE_CONNECTION;

    public String getMessageByType()
    {
        switch(this)
        {
            case BAD_MESSAGE_TYPE:
                return "Server Error: Unknown message type sent!";
            case RELOGIN_ERROR:
                return "Server Error: You are already authenticated, exit from session first!";
            case BAD_MESSAGE_DATA:
                return "Server Error: Bad message data!";
            case USER_ALREADY_EXIST:
                return "Server Error: This user already exist in system!";
            case USER_ALREADY_CONNECTED:
                return "Server Error: This user already online on server!";
            case USER_NOT_EXIST:
                return "Server Error: This user doesn't exist in system!";
            case CHAT_ALREADY_EXIST:
                return "Server Error: This chat already exist in system!";
            case CHAT_NOT_EXIST:
                return "Server Error: This chat doesn't exist in system!";
            case WRONG_PASSWORD:
                return "Server Error: Wrong password!";
            case USER_ALREADY_EXIST_IN_CHAT:
                return "Server Error: You are already in this chat!";
            case USER_NOT_EXIST_IN_CHAT:
                return "Server Error: You didn't connect to this chat!";
            case SESSION_NOT_EXIST:
                return "Server Error: Your session id doesn't exist! Authenticate first!";
            case SESSION_HAVE_ACTIVE_CONNECTION:
                return "Server Error: Session have another active connection!";
            default:
                return "null";
        }
    }
}
