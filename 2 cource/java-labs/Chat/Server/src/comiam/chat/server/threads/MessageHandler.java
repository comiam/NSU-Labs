package comiam.chat.server.threads;

import comiam.chat.server.connection.Connection;
import comiam.chat.server.connection.ConnectionTimers;
import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.Message;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.MessageFactory;
import comiam.chat.server.messages.MessageSender;
import comiam.chat.server.time.Date;
import comiam.chat.server.utils.ArgChecker;
import comiam.chat.server.utils.Hash;
import comiam.chat.server.utils.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import comiam.chat.server.messages.MessageNameConstants.UpdateType;
import static comiam.chat.server.messages.LogMessages.*;
import static comiam.chat.server.messages.MessageNameConstants.*;

public class MessageHandler implements Runnable
{
    private static final ArrayList<Pair<Socket, String>> messages = new ArrayList<>();

    public static void addNewMessage(Socket from, String message)
    {
        synchronized(messages)
        {
            messages.add(new Pair<>(from, message));
            messages.notifyAll();
        }
    }

    @Override
    public void run()
    {
        Pair<Socket, String> pair;

        while(!Thread.currentThread().isInterrupted())
        {
            pair = getMessage();

            if(pair == null)
                return;

            if(pair.getSecond().isEmpty())
            {
                Log.error("Backend thread: Message by " + pair.getFirst().getInetAddress() + " is too small!");
                MessageSender.sendError(pair.getFirst(), "Message too small!");
                continue;
            }

            if(ArgChecker.isNotNumeric(pair.getSecond().split("_")[0]))
            {
                Log.error("Backend thread: Invalid header size of message by " + pair.getFirst().getInetAddress());
                MessageSender.sendError(pair.getFirst(), "Invalid header size of message");
                continue;
            }

            int size = Integer.parseInt(pair.getSecond().split("_")[0]);
            String messageStr;

            try
            {
                messageStr = pair.getSecond().replaceFirst(size + "_", "").substring(0, size);
            }catch (Throwable e)
            {
                Log.error("Backend thread: Invalid header size of message by " + pair.getFirst().getInetAddress());
                MessageSender.sendError(pair.getFirst(), "Invalid header size of message");
                continue;
            }

            var message = MessageFactory.decodeXML(messageStr);

            if(message.getFirst() != null && message.getSecond() == null)
            {
                Log.error("Backend thread: Invalid message by " + pair.getFirst().getInetAddress());
                MessageSender.sendError(pair.getFirst(), "Invalid message: " + message.getFirst());
                continue;
            }

            handle(pair.getFirst(), message.getSecond());
        }
    }

    private Pair<Socket, String> getMessage()
    {
        synchronized(messages)
        {
            while(messages.size() == 0)
                try
                { messages.wait(); }
                catch(InterruptedException e)
                { return null; }
        }

        return messages.remove(0);
    }

    public void handle(Socket socket, Document message)
    {
        if(message.getDocumentElement() == null)
        {
            badMessageDataError(socket);
            return;
        }

        if(!message.getDocumentElement().getNodeName().equals(DEFAULT_MESSAGE_XML_HEADER_NAME))
        {
            badMessageDataError(socket);
            return;
        }

        String messageType = message.getDocumentElement().getAttributes().item(0).getTextContent().trim();

        if(message.getDocumentElement().getAttributes().getLength() != 1 || !isConstantName(messageType))
        {
            badMessageTypeError(socket);
            return;
        }

        if(!messageType.equals(SIGN_UP_MESSAGE) && !messageType.equals(SIGN_IN_MESSAGE) && Sessions.isClientAuthorized(socket))
        {
            unauthorizedRequestError(socket);
            return;
        }

        User clientUser;
        Chat chat;
        if((messageType.equals(SIGN_UP_MESSAGE) || messageType.equals(SIGN_IN_MESSAGE)) && (clientUser = Sessions.getSessionUser(socket)) != null)
        {
            reloginError(clientUser, socket);
            return;
        }

        NodeList list = message.getDocumentElement().getChildNodes();

        switch(messageType)
        {
            case SIGN_UP_MESSAGE:
            {
                String username, password;
                String[] res = parseAndCheck(socket, true, list, "name", "password");
                username = res[0];
                password = res[1];

                logMessageOp(socket, null, username, messageType);

                if(ServerData.containsUsername(username))
                {
                    userAlreadyExistError(username, socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                clientUser = new User(Hash.hashBytes(password.getBytes()), username, Date.getDate());
                ServerData.addNewUser(clientUser);
                Sessions.createNewSession(socket, clientUser);
                MessageSender.sendSuccess(socket, "Hello, " + username + " :)");

                logSuccessMessageOp(socket, null, username, messageType);
                break;
            }
            case SIGN_IN_MESSAGE:
            {
                String username, password;
                String[] res = parseAndCheck(socket, true, list, "name", "password");
                username = res[0];
                password = res[1];

                logMessageOp(socket, null, username, messageType);

                if(!ServerData.containsUsername(username))
                {
                    userNotExistError(username, socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(ServerData.getUserByName(username));

                if(!Hash.hashBytes(password.getBytes()).equals(clientUser.getPassHash()))
                {
                    wrongPasswordError(socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                Sessions.createNewSession(socket, clientUser);
                MessageSender.sendSuccess(socket, "Hello, " + username + " :)");
                MessageSender.broadcastUpdateFrom(UpdateType.ONLINE_UPDATE, clientUser);

                logSuccessMessageOp(socket, null, username, messageType);
                break;
            }
            case GET_CHATS_MESSAGE:
            {
                String chats = Objects.requireNonNull(MessageFactory.generateChatListMessage());

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), null, messageType);

                MessageSender.sendMessage(socket, chats);
                ConnectionTimers.zeroTimer(clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), null, messageType);
                break;
            }
            case GET_ONLINE_USERS_OF_CHAT_MESSAGE:
            {
                String chatName = parseAndCheck(socket, false, list, "name")[0];

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), chatName, messageType);

                if(!ServerData.containsChat(chatName))
                {
                    chatNotExistError(chatName, socket);
                    return;
                }

                String onlineUsers = MessageFactory.generateOnlineChatUsersListMessage(chatName);
                MessageSender.sendMessage(socket, onlineUsers);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), chatName, messageType);
                break;
            }
            case GET_USERS_OF_CHAT_MESSAGE:
            {
                String chatName = parseAndCheck(socket, false, list, "name")[0];

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), chatName, messageType);

                if(!ServerData.containsChat(chatName))
                {
                    chatNotExistError(chatName, socket);
                    return;
                }

                String users = MessageFactory.generateChatUsersListMessage(chatName);
                MessageSender.sendMessage(socket, users);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), chatName, messageType);
                break;
            }
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
            {
                String chatName = parseAndCheck(socket, false, list, "name")[0];

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), chatName, messageType);

                if(!ServerData.containsChat(chatName))
                {
                    chatNotExistError(chatName, socket);
                    return;
                }

                String messages = MessageFactory.generateChatMessageListMessage(chatName);
                MessageSender.sendMessage(socket, messages);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), chatName, messageType);
                break;
            }
            case DISCONNECT_MESSAGE:
            {
                if(!Sessions.isClientAuthorized(socket))
                {
                    unauthorizedRequestError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(),null, messageType);

                MessageSender.sendSuccess(socket, "Goodbye, " + clientUser.getUsername() + "!");
                Connection.disconnectClient(Sessions.getSessionUser(socket));
                MessageSender.broadcastUpdateFrom(UpdateType.ONLINE_UPDATE, clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), null, messageType);
                break;
            }
            case SEND_MESSAGE_MESSAGE:
            {
                String chatName, messageStr;
                String[] res = parseAndCheck(socket, false, list, "name", "message");
                chatName = res[0];
                messageStr = res[1];

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                logMessageOp(socket, clientUser.getUsername(), chatName, messageType);

                if(!ServerData.containsChat(chatName))
                {
                    chatNotExistError(chatName, socket);
                    return;
                }

                chat = Objects.requireNonNull(ServerData.getChatByName(chatName));
                chat.addMessage(new Message(messageStr, Date.getDate(), Sessions.getSessionUser(socket)));

                MessageSender.broadcastUpdateFrom(UpdateType.MESSAGE_UPDATE, clientUser);
                ConnectionTimers.zeroTimer(clientUser);
                MessageSender.sendSuccess(socket, "sent");

                logSuccessMessageOp(socket, clientUser.getUsername(), null, messageType);
                break;
            }
            case CREATE_CHAT_MESSAGE:
            {
                String chatName = parseAndCheck(socket, false, list, "name")[0];
                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                logMessageOp(socket, clientUser.getUsername(), chatName, messageType);

                if(ServerData.containsChat(chatName))
                {
                    chatAlreadyExistError(chatName, socket);
                    return;
                }

                ArrayList<User> users = new ArrayList<>();
                users.add(clientUser);

                chat = new Chat(chatName, users, null);
                ServerData.addNewChat(chat);

                MessageSender.broadcastUpdateFrom(UpdateType.USER_UPDATE, clientUser);
                ConnectionTimers.zeroTimer(clientUser);
                MessageSender.sendSuccess(socket, "hello in " + chatName + ":)");

                logSuccessMessageOp(socket, clientUser.getUsername(), chatName, messageType);
                break;
            }
            case CONNECT_TO_CHAT:
            {
                String chatName = parseAndCheck(socket, false, list, "name")[0];

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), chatName, messageType);

                if(!ServerData.containsChat(chatName))
                {
                    chatNotExistError(chatName, socket);
                    return;
                }

                chat = Objects.requireNonNull(ServerData.getChatByName(chatName));

                if(chat.containsUser(clientUser))
                {
                    userAlreadyExistInChatError(clientUser.getUsername(), chatName, socket);
                    return;
                }

                chat.addUser(Sessions.getSessionUser(socket));

                MessageSender.broadcastUpdateFrom(UpdateType.USER_UPDATE, clientUser);
                ConnectionTimers.zeroTimer(clientUser);
                MessageSender.sendSuccess(socket, "hello in " + chatName + ":)");

                logSuccessMessageOp(socket, clientUser.getUsername(), chatName, messageType);
                break;
            }
            default:
                badMessageDataError(socket);
        }
    }

    private String getNodeVal(NodeList list, int index)
    {
        return list.item(index).getTextContent().trim();
    }

    private String getNodeName(NodeList list, int index)
    {
        return list.item(index).getNodeName().trim();
    }

    private String[] parseAndCheck(Socket socket, boolean disconnectOnError, NodeList list, String... values)
    {
        if(values == null || values.length == 0)
            return null;

        if(list.getLength() != values.length)
        {
            badMessageDataError(socket);
            if(disconnectOnError)
                Connection.disconnectIfOnline(socket);
            return null;
        }

        String[] result = new String[values.length];

        for(int i = 0; i < values.length;i++)
        {
            if(!getNodeName(list, i).trim().equals(values[i]))
            {
                badMessageDataError(socket);
                if(disconnectOnError)
                    Connection.disconnectIfOnline(socket);
                return null;
            }

            if((result[i] = getNodeVal(list, i)).trim().isEmpty())
            {
                badFieldValueError(socket, values[i]);
                if(disconnectOnError)
                    Connection.disconnectIfOnline(socket);
                return null;
            }
        }

        return result;
    }
}
