package comiam.chat.client.connection;

import comiam.chat.client.connection.message.MessagePackage;
import comiam.chat.client.connection.message.MessageType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static comiam.chat.client.json.JSONCore.parseFromJSON;
import static comiam.chat.client.utils.ByteUtils.byteArrayToInt;
import static comiam.chat.client.utils.ByteUtils.reverse;

public class Connection
{
    public static boolean sendToServer(Socket connection, byte[] data)
    {
        try
        {
            connection.getOutputStream().write(data);
        } catch (IOException e)
        {
            return false;
        }
        return true;
    }

    public static MessagePackage receiveFromServer(Socket connection)
    {
        try
        {
            connection.setSoTimeout(5000);

            byte[] size = new byte[4];

            int s = connection.getInputStream().read(size);

            if(s < 4)
                return null;

            reverse(size);

            if(byteArrayToInt(size) <= 0)
                return null;

            byte[] msg = new byte[byteArrayToInt(size)];
            s = connection.getInputStream().read(msg);

            if(s < msg.length)
                return null;

            return parseFromJSON(new String(msg), MessagePackage.class);
        } catch(Throwable e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
