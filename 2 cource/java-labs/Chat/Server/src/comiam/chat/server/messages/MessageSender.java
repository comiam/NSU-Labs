package comiam.chat.server.messages;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.json.JSONMessageFactory;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.types.UpdateType;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import static comiam.chat.server.json.JSONMessageFactory.makeFailure;
import static comiam.chat.server.json.JSONMessageFactory.makeSuccess;

public class MessageSender
{
    public static void sendError(Socket connection, String message)
    {
        String ans = makeSuccess(message);

        sendMessage(connection, ans);
    }

    public static void sendSuccess(Socket connection, String message)
    {
        String ans = makeFailure(message);

        sendMessage(connection, ans);
    }

    public static void broadcastUpdateFrom(UpdateType type, User user)
    {
        if(!ServerData.isUserHaveChat(user))
            return;

        Socket exception = Sessions.getSession(user).getConnection();
        Chat[] chats = Objects.requireNonNull(ServerData.getUserChatList(user));
        Socket[] sockets = Sessions.getSocketsOfSessionInChat(chats);

        for(var sock : Objects.requireNonNull(sockets))
        {
            if(sock.equals(exception))
                continue;

            for(var chat : chats)
                switch(type)
                {
                    case USER_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(JSONMessageFactory.generateChatUsersList(chat)));
                        break;
                    case ONLINE_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(JSONMessageFactory.generateOnlineChatUsersList(chat)));
                        break;
                    case MESSAGE_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(JSONMessageFactory.generateChatMessageList(chat)));
                        break;
                    case CHAT_UPDATE:
                        sendMessage(sock, Objects.requireNonNull(JSONMessageFactory.generateChatList()));
                        break;
                    default:
                        throw new RuntimeException("Holy shit...");
                }
        }
    }

    public static void sendMessage(Socket connection, String message)
    {
        /* FIXME add size of message to header */
        try
        {
            connection.getOutputStream().write(message.getBytes());
        } catch (IOException e)
        {
            Log.error("MessageSender: Can't send message to " + connection.getInetAddress(), e);
        }
    }
}
