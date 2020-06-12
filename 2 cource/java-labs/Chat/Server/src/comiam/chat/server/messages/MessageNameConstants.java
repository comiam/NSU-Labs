package comiam.chat.server.messages;

public class MessageNameConstants
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
    public static final String CONNECT_TO_CHAT                  = "connectchat";
    public static final String DISCONNECT_MESSAGE               = "disconnect";

    /* Used on server only */
    public static final String DEFAULT_CONFIG_XML_HEADER_NAME  = "config";
    public static final String DEFAULT_MESSAGE_XML_HEADER_NAME = "command";
    public static final String SUCCESS_MESSAGE                 = "success";
    public static final String ERROR_MESSAGE                   = "error";
    public static final String NOTICE_MESSAGE                  = "notice";

    public enum UpdateType
    {
        ONLINE_UPDATE,
        MESSAGE_UPDATE,
        USER_UPDATE,
        CHAT_UPDATE
    }

    public static boolean isConstantName(String name)
    {
        return name.equals(SIGN_IN_MESSAGE) || name.equals(SIGN_UP_MESSAGE) || name.equals(GET_CHATS_MESSAGE) ||
                 name.equals(GET_USERS_OF_CHAT_MESSAGE) || name.equals(GET_MESSAGES_FROM_CHAT_MESSAGE) ||
                 name.equals(SEND_MESSAGE_MESSAGE) || name.equals(CREATE_CHAT_MESSAGE) || name.equals(DISCONNECT_MESSAGE) ||
                 name.equals(GET_ONLINE_USERS_OF_CHAT_MESSAGE) || name.equals(CONNECT_TO_CHAT);
    }
}
