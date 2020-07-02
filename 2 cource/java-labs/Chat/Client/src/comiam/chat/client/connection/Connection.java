package comiam.chat.client.connection;

import comiam.chat.client.connection.message.MessagePackage;

import java.net.Socket;

import static comiam.chat.client.json.JSONCore.parseFromJSON;
import static comiam.chat.client.utils.ByteUtils.byteArrayToInt;
import static comiam.chat.client.utils.ByteUtils.reverse;

public class Connection
{
    private static Socket currentConnection;
    private static final Object connectionLocker = new Object();

    public static void setCurrentConnection(Socket socket)
    {
        currentConnection = socket;
    }

    public static boolean sendToServer(byte[] data)
    {
        try
        {
            currentConnection.getOutputStream().write(data);
        } catch (Throwable e)
        {
            return false;
        }
        return true;
    }

    public static boolean haveMessageFromServer()
    {
        synchronized(connectionLocker)
        {
            try
            {
                return currentConnection.getInputStream().available() > 0;
            }catch(Throwable e)
            {
                return false;
            }
        }
    }

    public static MessagePackage receiveFromServer()
    {
        synchronized(connectionLocker)
        {
            try
            {
                currentConnection.setSoTimeout(5000);

                byte[] size = new byte[4];

                int s = currentConnection.getInputStream().read(size);

                if(s < 4)
                    return null;

                reverse(size);

                if(byteArrayToInt(size) <= 0)
                    return null;

                byte[] msg = new byte[byteArrayToInt(size)];
                s = currentConnection.getInputStream().read(msg);

                if(s < msg.length)
                    return null;

                return parseFromJSON(new String(msg), MessagePackage.class);
            } catch(Throwable e)
            {
                return null;
            }
        }
    }

    public static void closeConnection()
    {
        try
        {
            currentConnection.close();
        }catch(Throwable ignored){}
    }
}
