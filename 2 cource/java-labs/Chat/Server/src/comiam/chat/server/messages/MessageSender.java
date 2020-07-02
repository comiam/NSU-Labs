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

    public static void broadcastUpdateFrom(MessageType type, User user, Chat chat)
    {
        Socket exception = Sessions.getSession(user).getConnection();

        switch(type)
        {
            case MESSAGE_UPDATE:
                if(!ServerData.isUserHaveChat(user))
                    return;

                Socket[] sockets = Sessions.getSocketsOfSessionInChat(chat);

                if(sockets == null)
                    return;

                for(var sock : sockets)
                    if(!sock.equals(exception))
                        sendMessage(sock, makeNotice(JSONMessageFactory.generateChatMessageList(chat, true), type));
                break;
            case CHAT_UPDATE:
                if(Sessions.getAllSockets() != null)
                    for(var sock : Sessions.getAllSockets())
                        if(!sock.equals(exception))
                            sendMessage(sock, makeNotice(JSONMessageFactory.generateChatList(), type));
                break;
            default:
                throw new RuntimeException("Holy shit...");
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
        } catch(IOException e)
        {
            Log.error("MessageSender: Can't send message to " + connection.getInetAddress(), e);
        }
    }
}
