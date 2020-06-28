package comiam.chat.server.messages;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import comiam.chat.server.messages.MessageNameConstants.UpdateType;
import comiam.chat.server.xml.XMLMessageFactory;

public class MessageSender
{
    public static void sendError(Socket connection, String message)
    {
        String xml = XMLMessageFactory.generateSimpleMessage(MessageNameConstants.ERROR_MESSAGE, message);

        if(xml == null)
            throw new RuntimeException("Null message");
        sendMessage(connection, xml);
    }

    public static void sendSuccess(Socket connection, String message)
    {
        String xml = XMLMessageFactory.generateSimpleMessage(MessageNameConstants.SUCCESS_MESSAGE, message);

        if(xml == null)
            throw new RuntimeException("Null message");
        sendMessage(connection, xml);
    }

    public static void broadcastUpdateFrom(UpdateType type, User user)
    {
        if(!ServerData.isUserHaveChat(user))
            return;

        Socket exception = Sessions.getSession(user).getConnection();
        Chat[] chats = Objects.requireNonNull(ServerData.getUserChatList(user));
        String message = XMLMessageFactory.generateNoticeMessage(type.name(), chats.length);
        Socket[] sockets = Sessions.getSocketsOfSessionInChat(chats);

        for(var sock : Objects.requireNonNull(sockets))
        {
            if(sock.equals(exception))
                continue;

            sendMessage(sock, message);

            for(var chat : chats)
            {
                switch(type)
                {
                    case USER_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(XMLMessageFactory.generateChatUsersListMessage(chat)));
                        break;
                    case ONLINE_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(XMLMessageFactory.generateOnlineChatUsersListMessage(chat)));
                        break;
                    case MESSAGE_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(XMLMessageFactory.generateChatMessageListMessage(chat)));
                        break;
                    case CHAT_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(XMLMessageFactory.generateChatListMessage()));
                        break;
                    default:
                        throw new RuntimeException("Holy shit...");
                }
            }
        }
    }

    public static void sendMessage(Socket connection, String message)
    {
        try
        {
            connection.getOutputStream().write(message.getBytes());
        } catch (IOException e)
        {
            Log.error("MessageSender: Can't send message to " + connection.getInetAddress(), e);
        }
    }
}
