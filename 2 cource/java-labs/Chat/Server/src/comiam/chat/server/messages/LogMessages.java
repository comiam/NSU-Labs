package comiam.chat.server.messages;

import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;

import java.net.Socket;

public class LogMessages
{
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

    public static void badFieldValueError(Socket victim, String name)
    {
        Log.error("Backend thread: Bad field \"" + name + "\" in message from " + victim.getInetAddress() + "!");
        MessageSender.sendError(victim, "Server Error: Bad field \"" + name + "\"!");
    }

    public static void userAlreadyExistError(String username, Socket victim)
    {
        Log.error("Backend server: User " + username + " already exist in system! Disconnect " + victim.getInetAddress() + " from server...");
        MessageSender.sendError(victim, "Server Error: User " + username + " already exist in system!");
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
}
