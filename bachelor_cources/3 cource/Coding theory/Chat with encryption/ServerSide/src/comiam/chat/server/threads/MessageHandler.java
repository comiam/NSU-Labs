package comiam.chat.server.threads;

import comiam.chat.server.connection.Connection;
import comiam.chat.server.connection.ConnectionTimers;
import comiam.chat.server.core.ServerCore;
import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.Message;
import comiam.chat.server.data.units.User;
import comiam.chat.server.json.JSONMessageFactory;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.MessageSender;
import comiam.chat.server.messages.types.ErrorType;
import comiam.chat.server.messages.types.Request;
import comiam.chat.server.messages.types.MessageType;
import comiam.chat.server.security.AESCipher;
import comiam.chat.server.security.DiffieHellmanKeyGen;
import comiam.chat.server.security.KeyDB;
import comiam.chat.server.time.Date;
import comiam.chat.server.utils.Hash;
import comiam.chat.server.utils.Pair;

import java.math.BigInteger;
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
                MessageSender.sendError(pair.getFirst(), ErrorType.SMALL_MESSAGE_ERROR);
                continue;
            }

            Request message = parseFromJSON(pair.getSecond(), Request.class);

            if(message == null)
            {
                Log.error("Backend thread: Invalid message by " + pair.getFirst().getInetAddress());
                MessageSender.sendError(pair.getFirst(), ErrorType.BAD_MESSAGE_DATA);
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
            MessageSender.sendSuccess(socket, (Sessions.getSession(request.getSessionID()) != null) + "", false);
            return;
        }

        User clientUser;
        Chat chat;
        String name = request.getName();
        String password = request.getPass();
        String message = request.getMessage();
        String sessionID = request.getSessionID();
        int status;

        if(request.getType() != SIGN_UP_MESSAGE && request.getType() != SIGN_IN_MESSAGE && Sessions.isErrorStatus(status = Sessions.parseAndManageSession(sessionID, socket)))
        {
            invalidSessionParsingError(socket, sessionID, status);
            return;
        }

        if((request.getType() == SIGN_UP_MESSAGE || request.getType() == SIGN_IN_MESSAGE) && (clientUser = Sessions.getSessionUser(socket)) != null)
        {
            reloginError(clientUser, socket);
            return;
        }

        switch(request.getType())
        {
            case SIGN_IN_MESSAGE:
                if(objIsNull(name, password))
                {
                    badMessageDataError(socket);
                    Connection.disconnectClient(socket);
                    return;
                }

                logMessageOp(socket, name, null, request.getType());

                if(!ServerData.containsUsername(name))
                {
                    userNotExistError(name, socket);
                    Connection.disconnectClient(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(ServerData.getUserByName(name));

                if(Sessions.getSession(clientUser) != null)
                {
                    userAlreadyConnectedError(name, socket);
                    Connection.disconnectClient(socket);
                    return;
                }

                if(!Hash.hashBytes(password.getBytes()).equals(clientUser.getPassHash()))
                {
                    wrongPasswordError(socket);
                    Connection.disconnectClient(socket);
                    return;
                }

                sessionID = Sessions.createNewSession(socket, clientUser);
                MessageSender.sendSuccess(socket, sessionID, false);

                logSuccessMessageOp(socket, name, null, request.getType());
                break;
            case SIGN_UP_MESSAGE:
                if(objIsNull(name, password))
                {
                    badMessageDataError(socket);
                    Connection.disconnectClient(socket);
                    return;
                }

                logMessageOp(socket, name, null, request.getType());

                if(ServerData.containsUsername(name))
                {
                    userAlreadyExistError(name, socket);
                    Connection.disconnectClient(socket);
                    return;
                }

                clientUser = new User(Hash.hashBytes(password.getBytes()), name);
                ServerData.addNewUser(clientUser);

                sessionID = Sessions.createNewSession(socket, clientUser);
                MessageSender.sendSuccess(socket, sessionID, false);

                ServerCore.forceSaveDB();

                logSuccessMessageOp(socket, name, null, request.getType());
                break;
            case DIFFIE_HELLMAN_INIT:
                if(objIsNull(name, message))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                BigInteger generator, prime, publicA;

                String[] nums = message.split(";");

                if(nums.length != 3)
                {
                    badMessageDataError(socket);
                    return;
                }

                generator = new BigInteger(nums[0]);
                prime = new BigInteger(nums[1]);
                publicA = new BigInteger(nums[2]);

                BigInteger secretB = DiffieHellmanKeyGen.getSecretValue();
                BigInteger publicB = DiffieHellmanKeyGen.getPublicValue(generator, prime, secretB);

                KeyDB.emplaceKey(clientUser, DiffieHellmanKeyGen.getGeneratedKey(publicA, prime, secretB));

                MessageSender.sendSuccess(socket, publicB.toString(), false);
                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
            case GET_CHATS_MESSAGE:
                String chats = Objects.requireNonNull(JSONMessageFactory.generateChatList());

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                if(!KeyDB.containsKey(clientUser))
                {
                    haventEncrypriptionKeyError(socket);
                    return;
                }

                logMessageOp(socket, clientUser.getUsername(), null, request.getType());

                MessageSender.sendSuccess(socket, chats, true);
                ConnectionTimers.zeroTimer(clientUser);

                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
            case GET_USERS_OF_CHAT_MESSAGE:
                if(objIsNull(name))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                if(!KeyDB.containsKey(clientUser))
                {
                    haventEncrypriptionKeyError(socket);
                    return;
                }

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
                MessageSender.sendSuccess(socket, usersStr, true);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
                if(objIsNull(name))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(), name, request.getType());

                if(!KeyDB.containsKey(clientUser))
                {
                    haventEncrypriptionKeyError(socket);
                    return;
                }

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
                MessageSender.sendSuccess(socket, messages, true);

                ConnectionTimers.zeroTimer(clientUser);
                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case SEND_MESSAGE_MESSAGE:
                if(objIsNull(name, message))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                if(!KeyDB.containsKey(clientUser))
                {
                    haventEncrypriptionKeyError(socket);
                    return;
                }

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
                try
                {
                    message = AESCipher.decryptMsg(message, KeyDB.getKeyOf(clientUser));
                }catch (Throwable e)
                {
                    e.printStackTrace();
                }
                chat.addMessage(new Message(message, Date.getDate(), Objects.requireNonNull(Sessions.getSessionUser(socket)).getUsername()));

                MessageSender.sendSuccess(socket, "sent", true);
                MessageSender.broadcastUpdateFrom(MessageType.MESSAGE_UPDATE, clientUser, chat);
                ConnectionTimers.zeroTimer(clientUser);

                ServerCore.forceSaveDB();

                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
            case CREATE_CHAT_MESSAGE:
                if(objIsNull(name))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                if(!KeyDB.containsKey(clientUser))
                {
                    haventEncrypriptionKeyError(socket);
                    return;
                }

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

                MessageSender.sendSuccess(socket, "hello in " + name + ":)", true);

                MessageSender.broadcastUpdateFrom(MessageType.CHAT_UPDATE, clientUser, null);
                ConnectionTimers.zeroTimer(clientUser);

                ServerCore.forceSaveDB();

                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case CONNECT_TO_CHAT_MESSAGE:
                if(objIsNull(name))
                {
                    badMessageDataError(socket);
                    return;
                }

                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));

                if(!KeyDB.containsKey(clientUser))
                {
                    haventEncrypriptionKeyError(socket);
                    return;
                }

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
                MessageSender.sendSuccess(socket, "hello in " + name + ":)", true);

                MessageSender.broadcastUpdateFrom(MessageType.CHAT_UPDATE, clientUser, null);
                MessageSender.broadcastUpdateFrom(MessageType.MESSAGE_UPDATE, clientUser, chat);

                ConnectionTimers.zeroTimer(clientUser);

                ServerCore.forceSaveDB();

                logSuccessMessageOp(socket, clientUser.getUsername(), name, request.getType());
                break;
            case DISCONNECT_MESSAGE:
                clientUser = Objects.requireNonNull(Sessions.getSessionUser(socket));
                logMessageOp(socket, clientUser.getUsername(),null, request.getType());

                MessageSender.sendSuccess(socket, "Goodbye, " + clientUser.getUsername() + "!", false);
                Connection.disconnectClient(Sessions.getSessionUser(socket));

                logSuccessMessageOp(socket, clientUser.getUsername(), null, request.getType());
                break;
        }
    }

    private boolean objIsNull(Object... objects)
    {
        for(var obj : objects)
            if(obj == null)
                return true;
        return false;
    }
}
