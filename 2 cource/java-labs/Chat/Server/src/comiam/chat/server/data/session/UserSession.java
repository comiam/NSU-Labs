package comiam.chat.server.data.session;

import comiam.chat.server.time.Date;
import comiam.chat.server.utils.Hash;

import java.net.Socket;

public class UserSession
{
    private Socket activeConnection;
    private String id;

    public static UserSession createNewSession(Socket activeConnection)
    {
        UserSession session = new UserSession();
        session.activeConnection = activeConnection;
        session.id = Hash.hashBytes((Date.getDateForSession() + activeConnection.hashCode()).getBytes());

        return session;
    }

    public boolean haveActiveConnection()
    {
        return activeConnection != null && !activeConnection.isClosed();
    }

    public String getID()
    {
        return id;
    }

    public boolean idEquals(String id)
    {
        return this.id.equals(id);
    }

    public Socket getConnection()
    {
        return activeConnection;
    }

    public void replaceConnectionSocket(Socket newSock)
    {
        try
        {
            activeConnection.close();
        }catch(Throwable ignored){}

        activeConnection = newSock;
    }
}
