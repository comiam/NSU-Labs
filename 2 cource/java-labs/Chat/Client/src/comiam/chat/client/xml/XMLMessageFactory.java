package comiam.chat.client.xml;

import static comiam.chat.client.client.MessageConstants.*;
import static comiam.chat.client.client.MessageConstants.DISCONNECT_MESSAGE;

public class XMLMessageFactory
{
    public enum CommandType
    {
        SIGN_IN_MESSAGE,
        SIGN_UP_MESSAGE,
        GET_CHATS_MESSAGE,
        GET_USERS_OF_CHAT_MESSAGE,
        GET_ONLINE_USERS_OF_CHAT_MESSAGE,
        GET_MESSAGES_FROM_CHAT_MESSAGE,
        SEND_MESSAGE_MESSAGE,
        CREATE_CHAT_MESSAGE,
        CONNECT_TO_CHAT_MESSAGE,
        DISCONNECT_MESSAGE
    }

    public static String generateMessage(CommandType type, String... data)
    {
        String res = "<" + DEFAULT_MESSAGE_XML_HEADER_NAME + " type = ";

        switch(type)
        {
            case SIGN_IN_MESSAGE:
                res += SIGN_IN_MESSAGE + ">";
                res += "<name>" + data[0] + "</name>";
                res += "<password>" + data[1] + "</password>";
                break;
            case SIGN_UP_MESSAGE:
                res += SIGN_UP_MESSAGE + ">";
                res += "<name>" + data[0] + "</name>";
                res += "<password>" + data[1] + "</password>";
                break;
            case GET_CHATS_MESSAGE:
                res += GET_CHATS_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                break;
            case GET_USERS_OF_CHAT_MESSAGE:
                res += GET_USERS_OF_CHAT_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                res += "<name>" + data[1] + "</name>";
                break;
            case GET_ONLINE_USERS_OF_CHAT_MESSAGE:
                res += GET_ONLINE_USERS_OF_CHAT_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                res += "<name>" + data[1] + "</name>";
                break;
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
                res += GET_MESSAGES_FROM_CHAT_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                res += "<name>" + data[1] + "</name>";
                break;
            case SEND_MESSAGE_MESSAGE:
                res += SEND_MESSAGE_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                res += "<name>" + data[1] + "</name>";
                res += "<message>" + data[2] + "</message>";
                break;
            case CREATE_CHAT_MESSAGE:
                res += CREATE_CHAT_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                res += "<name>" + data[1] + "</name>";
                break;
            case CONNECT_TO_CHAT_MESSAGE:
                res += CONNECT_TO_CHAT_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                res += "<name>" + data[1] + "</name>";
                break;
            case DISCONNECT_MESSAGE:
                res += DISCONNECT_MESSAGE + ">";
                res += "<sessionID>" + data[0] + "</sessionID>";
                break;
            default:
        }
        res += "</" + DEFAULT_MESSAGE_XML_HEADER_NAME + ">";
        return res;
    }
}
