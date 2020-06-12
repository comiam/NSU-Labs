package comiam.chat.server.messages;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import comiam.chat.server.messages.MessageNameConstants.UpdateType;

public class MessageSender
{
    public static void sendError(Socket connection, String message)
    {
        String xml = MessageFactory.generateSimpleMessage(MessageNameConstants.ERROR_MESSAGE, message);

        if(xml == null)
            throw new RuntimeException("Null message");
        sendMessage(connection, xml);
    }

    public static void sendSuccess(Socket connection, String message)
    {
        String xml = MessageFactory.generateSimpleMessage(MessageNameConstants.SUCCESS_MESSAGE, message);

        if(xml == null)
            throw new RuntimeException("Null message");
        sendMessage(connection, xml);
    }

    public static void broadcastUpdateFrom(UpdateType type, User user)
    {
        if(!ServerData.isUserHaveChat(user))
            return;

        Socket exception = Sessions.getSessionSocket(user);
        Chat[] chats = Objects.requireNonNull(ServerData.getUserChatList(user));
        String message = MessageFactory.generateNoticeMessage(type.name(), chats.length);
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
                        sendMessage(sock, Objects.requireNonNull(MessageFactory.generateChatUsersListMessage(chat)));
                        break;
                    case ONLINE_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(MessageFactory.generateOnlineChatUsersListMessage(chat)));
                        break;
                    case MESSAGE_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(MessageFactory.generateChatMessageListMessage(chat)));
                        break;
                    case CHAT_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(MessageFactory.generateChatListMessage()));
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
