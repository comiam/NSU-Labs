package comiam.chat.server.data.session;

import comiam.chat.server.time.Date;
import comiam.chat.server.utils.Hash;

import java.net.Socket;
import java.nio.ByteBuffer;

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
        if(activeConnection == null || activeConnection.isClosed())
            return false;

        try
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(16);
            if(activeConnection.getChannel().read(buffer) == -1)
                return false;
        } catch (Exception e)
        {
            return false;
        }
        return true;
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
