package comiam.chat.server.threads;

import comiam.chat.server.connection.Connection;
import comiam.chat.server.connection.ConnectionTimers;
import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.Message;
import comiam.chat.server.data.units.User;
import comiam.chat.server.json.JSONMessageFactory;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.MessageSender;
import comiam.chat.server.messages.types.Request;
import comiam.chat.server.messages.types.MessageType;
import comiam.chat.server.time.Date;
import comiam.chat.server.utils.Hash;
import comiam.chat.server.utils.Pair;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import static comiam.chat.server.json.JSONCore.parseFromJSON;
import static comiam.chat.server.messages.LogMessages.*;
import static comiam.chat.server.messages.types.RequestType.*;

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

            Request message = parseFromJSON(pair.getSecond(), Request.class);

            if(message == null)
            {
                Log.error("Backend thread: Invalid message by " + pair.getFirst().getInetAddress());
                MessageSender.sendError(pair.getFirst(), "Invalid message!");
                continue;
            }

            handle(pair.getFirst(), message);
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

    public void handle(Socket socket, Request request)
    {
        if(request.getType() == null)
        {
            badMessageTypeError(socket);
            return;
        }

        if(request.getType() == CHECK_CONNECTED_MESSAGE)
        {
            logMessageOp(socket, null, null, request.getType());
            MessageSender.sendSuccess(socket, Sessions.isClientAuthorized(socket) + "");
            return;
        }

        if(request.getType() != SIGN_UP_MESSAGE && request.getType() != SIGN_IN_MESSAGE && !Sessions.isClientAuthorized(socket))
        {
            unauthorizedRequestError(socket);
            return;
        }

        User clientUser;
        Chat chat;
        int status;

        if((request.getType() == SIGN_UP_MESSAGE || request.getType() == SIGN_IN_MESSAGE) && (clientUser = Sessions.getSessionUser(socket)) != null)
        {
            reloginError(clientUser, socket);
            return;
        }

        String name = request.getName();
        String password = request.getPass();
        String message = request.getMessage();
        String sessionID = request.getSessionID();

        if(request.getType() != SIGN_UP_MESSAGE && request.getType() != SIGN_IN_MESSAGE && Sessions.isErrorStatus(status = Sessions.parseAndManageSession(sessionID, socket)))
        {
            invalidSessionParsingError(sessionID, status);
            return;
        }

        switch(request.getType())
        {
            case SIGN_IN_MESSAGE:
                if(checkIsNull(name, password))
                {
                    badMessageDataError(socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                logMessageOp(socket, name, null, request.getType());

                if(!ServerData.containsUsername(name))
                {
                    userNotExistError(name, socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(ServerData.getUserByName(name));

                if(Sessions.getSession(clientUser) != null)
                {
                    userAlreadyConnectedError(name, socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                if(!Hash.hashBytes(password.getBytes()).equals(clientUser.getPassHash()))
                {
                    wrongPasswordError(socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                sessionID = Sessions.createNewSession(socket, clientUser);
                MessageSender.sendSuccess(socket, sessionID);

                logSuccessMessageOp(socket, name, null, request.getType());
                break;
            case SIGN_UP_MESSAGE:
                if(checkIsNull(name, password))
                {
                    badMessageDataError(socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                logMessageOp(socket, name, null, request.getType());

                if(ServerData.containsUsername(name))
                {
                    userAlreadyExistError(name, socket);
                    Connection.disconnectIfOnline(socket);
                    return;
                }

                clientUser = new User(Hash.hashBytes(password.getBytes()), name);
                ServerData.addNewUser(clientUser);
                sessionID = Sessions.createNewSession(socket, clientUser);
                MessageSender.sendSuccess(socket, sessionID);

                logSuccessMessageOp(socket, name, null, request.getType());
                break;
            case GET_CHATS_MESSAGE:
                if(checkIsNull(sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                String chats = Objects.requireNonNull(JSONMessageFactory.generateChatList());

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), null, request.getType());

                MessageSender.sendSuccess(socket, chats);
                ConnectionTimers.zeroTimer(clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
            case GET_USERS_OF_CHAT_MESSAGE:
                if(checkIsNull(name, sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                if(!ServerData.containsChat(name))
                {
                    chatNotExistError(name, socket);
                    return;
                }

                if(!Objects.requireNonNull(ServerData.getChatByName(name)).containsUser(clientUser))
                {
                    userNotExistInChatError(clientUser.getUsername(), name, socket);
                    return;
                }

                String usersStr = JSONMessageFactory.generateChatUsersList(Objects.requireNonNull(ServerData.getChatByName(name)));
                MessageSender.sendSuccess(socket, usersStr);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
                if(checkIsNull(name, sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                if(!ServerData.containsChat(name))
                {
                    chatNotExistError(name, socket);
                    return;
                }

                if(!Objects.requireNonNull(ServerData.getChatByName(name)).containsUser(clientUser))
                {
                    userNotExistInChatError(clientUser.getUsername(), name, socket);
                    return;
                }

                String messages = JSONMessageFactory.generateChatMessageList(Objects.requireNonNull(ServerData.getChatByName(name)), false);
                MessageSender.sendSuccess(socket, messages);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case SEND_MESSAGE_MESSAGE:
                if(checkIsNull(name, message, sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                if(!ServerData.containsChat(name))
                {
                    chatNotExistError(name, socket);
                    return;
                }

                chat = Objects.requireNonNull(ServerData.getChatByName(name));

                if(!chat.containsUser(clientUser))
                {
                    userNotExistInChatError(clientUser.getUsername(), name, socket);
                    return;
                }

                chat.addMessage(new Message(message, Date.getDate(), Objects.requireNonNull(Sessions.getSessionUser(socket)).getUsername()));

                MessageSender.sendSuccess(socket, "sent");
                MessageSender.broadcastUpdateFrom(MessageType.MESSAGE_UPDATE, clientUser, chat);
                ConnectionTimers.zeroTimer(clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
            case CREATE_CHAT_MESSAGE:
                if(checkIsNull(name, sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                if(ServerData.containsChat(name))
                {
                    chatAlreadyExistError(name, socket);
                    return;
                }

                ArrayList<User> users = new ArrayList<>();
                users.add(clientUser);

                chat = new Chat(name, users, null);
                ServerData.addNewChat(chat);

                chat.addMessage(new Message("Wow, " + Objects.requireNonNull(Sessions.getSessionUser(socket)).getUsername() + " is there!", Date.getDate(), ServerData.getServerNotifier().getUsername()));

                MessageSender.sendSuccess(socket, "hello in " + name + ":)");

                MessageSender.broadcastUpdateFrom(MessageType.CHAT_UPDATE, clientUser, null);
                ConnectionTimers.zeroTimer(clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case CONNECT_TO_CHAT_MESSAGE:
                if(checkIsNull(name, sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                if(!ServerData.containsChat(name))
                {
                    chatNotExistError(name, socket);
                    return;
                }

                chat = Objects.requireNonNull(ServerData.getChatByName(name));

                if(chat.containsUser(clientUser))
                {
                    userAlreadyExistInChatError(clientUser.getUsername(), name, socket);
                    return;
                }

                chat.addUser(clientUser);

                chat.addMessage(new Message("Wow, " + Objects.requireNonNull(Sessions.getSessionUser(socket)).getUsername() + " is there!", Date.getDate(), ServerData.getServerNotifier().getUsername()));
                MessageSender.sendSuccess(socket, "hello in " + name + ":)");

                MessageSender.broadcastUpdateFrom(MessageType.CHAT_UPDATE, clientUser, null);
                MessageSender.broadcastUpdateFrom(MessageType.MESSAGE_UPDATE, clientUser, chat);

                ConnectionTimers.zeroTimer(clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case DISCONNECT_MESSAGE:
                if(checkIsNull(sessionID))
                {
                    badMessageDataError(socket);
                    return;
                }

                if(!Sessions.isClientAuthorized(socket))
                {
                    unauthorizedRequestError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(),null, request.getType());

                MessageSender.sendSuccess(socket, "Goodbye, " + clientUser.getUsername() + "!");
                Connection.disconnectClient(Sessions.getSessionUser(socket));

                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
        }
    }

    private boolean checkIsNull(Object... objects)
    {
        for(var obj : objects)
            if(obj == null)
                return true;
        return false;
    }
}
