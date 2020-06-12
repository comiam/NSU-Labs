package comiam.chat.server.data;

import comiam.chat.server.connection.ConnectionTimers;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Sessions
{
    private static final HashMap<User, Socket> sessions = new HashMap<>();

    public static boolean isClientAuthorized(Socket connection)
    {
        return getSessionUser(connection) != null;
    }

    public static boolean isUserAuthorized(User user)
    {
        return getSessionSocket(user) != null;
    }

    public static synchronized Socket getSessionSocket(User user)
    {
        return sessions.getOrDefault(user, null);
    }

    public static synchronized User getSessionUser(Socket socket)
    {
        for(var key : sessions.keySet())
            if(sessions.get(key).equals(socket))
                return key;

        return null;
    }

    public static synchronized Socket[] getSocketsOfSessionInChat(Chat[] chats)
    {
        ArrayList<Socket> sockets = new ArrayList<>();

        for(var chat : chats)
            for(var user : chat.getUsers())
                if(sessions.containsKey(user))
                    sockets.add(sessions.get(user));

        return sockets.isEmpty() ? null : (Socket[]) sockets.toArray();
    }

    /**
     * Do not use this.
     * It used in Connection.disconnectClient()
     *
     * @param user - user, which we delete from session data
     * @see comiam.chat.server.connection.Connection
     */
    public static synchronized void deleteSession(User user)
    {
        sessions.remove(user);
    }

    public static synchronized void createNewSession(Socket socket, User user)
    {
        sessions.put(user, socket);
        ConnectionTimers.putNewTimer(user);
    }
}
