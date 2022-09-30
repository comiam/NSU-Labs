package comiam.chat.server.connection;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.time.Timer;

import java.net.Socket;
import java.util.HashMap;

import static comiam.chat.server.core.GlobalConstants.AUTHENTICATION_TIMEOUT;
import static comiam.chat.server.core.GlobalConstants.DEFAULT_TIMEOUT_IN_MINUTES;

public class ConnectionTimers
{
    private static final HashMap<User, Long> timers = new HashMap<>();

    public static void putNewTimer(User user)
    {
        synchronized(timers)
        {
            timers.put(user, 0L);
        }
    }

    public static void removeTimer(User user)
    {
        synchronized(timers)
        {
            if(user == null || !timers.containsKey(user))
                return;

            timers.remove(user);
        }
    }

    private static boolean incTimers()
    {
        synchronized(timers)
        {
            timers.replaceAll((k, v) -> timers.get(k) + Timer.MILLISECOND);
        }
        return true;
    }

    public static void addAuthTimer(Socket connection)
    {
        Timer.subscribeEvent(() -> {
            if(!connection.isClosed() && Sessions.getSession(connection) == null)
            {
                Log.error("Backend thread: Authentication with " + connection.getInetAddress() + ": timed out. Disconnect from server...");
                Connection.disconnectClient(connection);
            }

            return false;
        }, AUTHENTICATION_TIMEOUT);
    }

    public static void zeroTimer(User user)
    {
        synchronized(timers)
        {
            timers.put(user, 0L);
        }
    }

    public static void setTimerHandler()
    {
        Timer.subscribeEvent(ConnectionTimers::incTimers, Timer.SECOND);
        Timer.subscribeEvent(() -> {
            for (var user : ServerData.getUsers())
                if(Sessions.getSession(user) != null && timers.get(user) >= DEFAULT_TIMEOUT_IN_MINUTES)
                    Connection.disconnectClient(user);
            return true;
        }, 5 * Timer.MINUTE);
        Timer.start();
    }
}
