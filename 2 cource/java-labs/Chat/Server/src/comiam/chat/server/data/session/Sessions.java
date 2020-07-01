package comiam.chat.server.data.session;

import comiam.chat.server.connection.ConnectionTimers;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static comiam.chat.server.data.session.SessionConstants.*;

public class Sessions
{
    private static final HashMap<User, UserSession> sessions = new HashMap<>();

    public static boolean isClientAuthorized(Socket connection)
    {
        return getSession(connection) != null && getSession(connection).haveActiveConnection();
    }

    public static boolean isUserAuthorized(User user)
    {
        return getSession(user) != null && getSession(user).haveActiveConnection();
    }

    public static synchronized UserSession getSession(User user)
    {
        return sessions.getOrDefault(user, null);
    }

    public static synchronized UserSession getSession(Socket connection)
    {
        return getSession(getSessionUser(connection));
    }

    public static synchronized UserSession getSession(String ID)
    {
        for(var key : sessions.keySet())
            if(sessions.get(key).idEquals(ID))
                return sessions.get(key);
        return null;
    }

    public static synchronized User getSessionUser(Socket socket)
    {
        for(var key : sessions.keySet())
            if(sessions.get(key).getConnection().equals(socket))
                return key;

        return null;
    }

    public static synchronized Socket[] getSocketsOfSessionInChat(Chat[] chats)
    {
        ArrayList<Socket> sockets = new ArrayList<>();

        for(var chat : chats)
            for(var user : chat.getUsers())
                if(sessions.containsKey(user))
                    sockets.add(sessions.get(user).getConnection());

        return sockets.isEmpty() ? null : (Socket[]) sockets.toArray(new Socket[sockets.size()]);
    }

    public static synchronized int parseAndManageSession(String sessionID, Socket connection)
    {
        if(sessionID == null)
            return SESSION_NOT_EXIST;

        if(!haveSessionID(sessionID))
            return SESSION_NOT_EXIST;

        UserSession userSession = getSession(sessionID);

        assert userSession != null;
        if(userSession.haveActiveConnection() && !userSession.getConnection().equals(connection))
            return SESSION_HAVE_ACTIVE_CONNECTION;

        if(!userSession.haveActiveConnection())
            userSession.replaceConnectionSocket(connection);

        return SESSION_IS_VALID;
    }

    public static synchronized boolean isErrorStatus(int status)
    {
        return status != SESSION_IS_VALID;
    }

    public static synchronized boolean haveSessionID(String ID)
    {
        return getSession(ID) != null;
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
        ConnectionTimers.removeTimer(user);
    }

    public static synchronized String createNewSession(Socket socket, User user)
    {
        UserSession session = UserSession.createNewSession(socket);
        String id = session.getID();


        sessions.put(user, session);
        ConnectionTimers.putNewTimer(user);

        return id;
    }
}
