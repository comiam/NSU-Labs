package comiam.chat.server.messages;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.json.JSONMessageFactory;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.types.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Objects;

import static comiam.chat.server.json.JSONMessageFactory.*;
import static comiam.chat.server.utils.ByteUtils.concatenate;
import static comiam.chat.server.utils.ByteUtils.intToByteArray;

public class MessageSender
{
    public static void sendError(Socket connection, String message)
    {
        String ans = makeFailure(message);

        sendMessage(connection, ans);
    }

    public static void sendSuccess(Socket connection, String message)
    {
        String ans = makeSuccess(message);

        sendMessage(connection, ans);
    }

    public static void sendDisconnect(Socket connection)
    {
        String ans = makeDisconnect();

        sendMessage(connection, ans);
    }

    public static void broadcastUpdateFrom(MessageType type, User user)
    {
        if(!ServerData.isUserHaveChat(user))
            return;

        Socket exception = Sessions.getSession(user).getConnection();
        Chat[] chats = Objects.requireNonNull(ServerData.getUserChatList(user));
        Socket[] sockets = Sessions.getSocketsOfSessionInChat(chats);

        if(sockets == null)
            return;

        for(var sock : sockets)
        {
            if(sock.equals(exception))
                continue;

            for(var chat : chats)
                switch(type)
                {
                    case USER_UPDATE:
                        sendMessage(sock, makeNotice(JSONMessageFactory.generateChatUsersList(chat), type));
                        break;
                    case MESSAGE_UPDATE:
                        sendMessage(sock, makeNotice(JSONMessageFactory.generateChatMessageList(chat), type));
                        break;
                    case CHAT_UPDATE:
                        sendMessage(sock, makeNotice(JSONMessageFactory.generateChatList(), type));
                        break;
                    default:
                        throw new RuntimeException("Holy shit...");
                }
        }
    }

    private static void sendMessage(Socket connection, String message)
    {
        byte[] messageByte = message.getBytes();
        byte[] headerSize = intToByteArray(messageByte.length);

        byte[] messagePackage = concatenate(headerSize, messageByte);
        try
        {
            connection.getChannel().write(ByteBuffer.wrap(messagePackage));
        } catch (IOException e)
        {
            Log.error("MessageSender: Can't send message to " + connection.getInetAddress(), e);
        }
    }
}
