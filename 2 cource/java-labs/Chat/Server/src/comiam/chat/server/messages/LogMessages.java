package comiam.chat.server.messages;

import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.types.RequestType;

import java.net.Socket;

import static comiam.chat.server.data.session.SessionConstants.SESSION_HAVE_ACTIVE_CONNECTION;
import static comiam.chat.server.data.session.SessionConstants.SESSION_NOT_EXIST;

public class LogMessages
{
    public static void logMessageOp(Socket socket, String username, String param, RequestType type)
    {
        switch(type)
        {
            case CHECK_CONNECTED_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + " checking connection...");
                break;
            case SIGN_IN_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + " tries to sign in as " + username + ".");
                break;
            case SIGN_UP_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + " tries to sign up as " + username + ".");
                break;
            case CONNECT_TO_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to connect to chat " + param + ".");
                break;
            case GET_CHATS_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to get chat list.");
                break;
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to get messages from chat " + param + ".");
                break;
            case GET_USERS_OF_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to get users list from chat " + param + ".");
                break;
            case CREATE_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to create chat " + param + ".");
                break;
            case DISCONNECT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to disconnect.");
                break;
            case SEND_MESSAGE_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") tries to send message.");
                break;
        }
    }

    public static void logSuccessMessageOp(Socket socket, String username, String param, RequestType type)
    {
        switch(type)
        {
            case SIGN_IN_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + " signed in successfully as " + username + ".");
                break;
            case SIGN_UP_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + " signed up successfully as " + username + ".");
                break;
            case CONNECT_TO_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully connected to chat " + param + ".");
                break;
            case GET_CHATS_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully got chat list.");
                break;
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully got messages from chat " + param + ".");
                break;
            case GET_USERS_OF_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully got users list from chat " + param + ".");
                break;
            case CREATE_CHAT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully created chat " + param + ".");
                break;
            case DISCONNECT_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully disconnected.");
                break;
            case SEND_MESSAGE_MESSAGE:
                Log.info("Backend thread: Client " + socket.getInetAddress() + "(" + username + ") successfully sent message.");
                break;
        }
    }

    public static void badMessageTypeError(Socket victim)
    {
        Log.error("Backend thread: Unknown message type sent from " + victim.getInetAddress());
        MessageSender.sendError(victim, "Server Error: Unknown message type!");
    }

    public static void unauthorizedRequestError(Socket victim)
    {
        Log.error("Backend thread: Unauthorized request from " + victim.getInetAddress() + "! Disconnect from server...");
        MessageSender.sendError(victim, "Server Error: Authenticate first!");
    }

    public static void reloginError(User user, Socket victim)
    {
        Log.error("Backend thread: Trying to authenticate to " + user.getUsername() + ": this user client is authenticated!");
        MessageSender.sendError(victim, "Server Error: You are already authenticated, exit from session first!");
    }

    public static void badMessageDataError(Socket victim)
    {
        Log.error("Backend thread: Bad message data in message from " + victim.getInetAddress() + "!");
        MessageSender.sendError(victim, "Server Error: Bad message data!");
    }

    public static void userAlreadyExistError(String username, Socket victim)
    {
        Log.error("Backend server: User " + username + " already exist in system! Disconnect " + victim.getInetAddress() + " from server...");
        MessageSender.sendError(victim, "Server Error: User " + username + " already exist in system!");
    }

    public static void userAlreadyConnectedError(String username, Socket victim)
    {
        Log.error("Backend server: User " + username + " already connected in system! Disconnect " + victim.getInetAddress() + " from server...");
        MessageSender.sendError(victim, "Server Error: User " + username + " already connected in chat!");
    }

    public static void userNotExistError(String username, Socket victim)
    {
        Log.error("Backend server: User " + username + " doesn't exist in system! Disconnect " + victim.getInetAddress() + " from server...");
        MessageSender.sendError(victim, "Server Error: User " + username + " doesn't exist in system!");
    }

    public static void chatAlreadyExistError(String chatName, Socket victim)
    {
        Log.error("Backend server: Chat " + chatName + " already exist in system!");
        MessageSender.sendError(victim, "Server Error: Chat " + chatName + " already exist in system!");
    }

    public static void chatNotExistError(String chatName, Socket victim)
    {
        Log.error("Backend server: Chat " + chatName + " doesn't exist in system!");
        MessageSender.sendError(victim, "Server Error: Chat " + chatName + " doesn't exist in system!");
    }

    public static void wrongPasswordError(Socket victim)
    {
        Log.error("Backend server: Wrong password on signing in! Disconnect " + victim.getInetAddress() + " from server...");
        MessageSender.sendError(victim, "Server Error: Wrong password!");
    }

    public static void userAlreadyExistInChatError(String username, String chat, Socket victim)
    {
        Log.error("Backend server: User " + username + " already exist in chat " + chat + "!");
        MessageSender.sendError(victim, "Server Error: You are already in this chat!");
    }

    public static void userNotExistInChatError(String username, String chat, Socket victim)
    {
        Log.error("Backend server: User " + username + " not exist in chat " + chat + "!");
        MessageSender.sendError(victim, "Server Error: You don't connected to this chat!");
    }

    public static void invalidSessionParsingError(String sessionID, int error)
    {
        Log.error("Backend server: invalid session " + sessionID + ":");

        switch(error)
        {
            case SESSION_NOT_EXIST:
                Log.error("Session id doesn't exist!");
                break;
            case SESSION_HAVE_ACTIVE_CONNECTION:
                Log.error("Session have another connection!");
                break;
            default:
                Log.error("null");
        }
    }
}
