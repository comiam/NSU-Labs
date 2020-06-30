package comiam.chat.client.client;

public class MessageConstants
{
    /* Used in clients */
    public static final String SIGN_IN_MESSAGE                  = "signin";
    public static final String SIGN_UP_MESSAGE                  = "signup";
    public static final String GET_CHATS_MESSAGE                = "getchats";
    public static final String GET_USERS_OF_CHAT_MESSAGE        = "getusers";
    public static final String GET_ONLINE_USERS_OF_CHAT_MESSAGE = "getonlineusers";
    public static final String GET_MESSAGES_FROM_CHAT_MESSAGE   = "getmessages";
    public static final String SEND_MESSAGE_MESSAGE             = "sendmessage";
    public static final String CREATE_CHAT_MESSAGE              = "createchat";
    public static final String CONNECT_TO_CHAT_MESSAGE          = "connectchat";
    public static final String DISCONNECT_MESSAGE               = "disconnect";

    /* Used on server only */
    public static final String DEFAULT_MESSAGE_XML_HEADER_NAME  = "command";
}
