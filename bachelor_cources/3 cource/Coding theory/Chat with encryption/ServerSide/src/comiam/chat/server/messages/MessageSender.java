package comiam.chat.server.messages;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.json.JSONCore;
import comiam.chat.server.json.JSONMessageFactory;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.types.ErrorType;
import comiam.chat.server.messages.types.MessageType;

import java.net.Socket;
import java.nio.ByteBuffer;

import static comiam.chat.server.json.JSONMessageFactory.*;
import static comiam.chat.server.security.CryptUtil.encrypt;
import static comiam.chat.server.utils.ByteUtils.concatenate;
import static comiam.chat.server.utils.ByteUtils.intToByteArray;

public class MessageSender
{
    public static void sendError(Socket connection, ErrorType errorType)
    {
        String ans = makeFailure(encrypt(Sessions.getSessionUser(connection), JSONCore.saveToJSON(errorType)));

        sendMessage(connection, ans);
    }

    public static void sendSuccess(Socket connection, String message, boolean encrypt)
    {
        String ans = makeSuccess(encrypt ? encrypt(Sessions.getSessionUser(connection), message) : message);

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
                        sendMessage(sock, makeNotice(encrypt(Sessions.getSessionUser(sock), JSONMessageFactory.generateChatMessageList(chat, true)), type));
                break;
            case CHAT_UPDATE:
                if(Sessions.getAllSockets() != null)
                    for(var sock : Sessions.getAllSockets())
                        if(!sock.equals(exception))
                            sendMessage(sock, makeNotice(encrypt(Sessions.getSessionUser(sock), JSONMessageFactory.generateChatList()), type));
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
        } catch(Throwable e)
        {
            Log.error("MessageSender: Can't send message to " + connection.getInetAddress(), e);
        }
    }
}
