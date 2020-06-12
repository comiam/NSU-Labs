package comiam.chat.server.connection;

import comiam.chat.server.data.Sessions;
import comiam.chat.server.data.units.User;
import comiam.chat.server.threads.InputHandler;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Connection
{
    public static void disconnectIfOnline(Socket socket)
    {
        if(!Sessions.isClientAuthorized(socket))
            disconnectClient(socket);
    }

    public static void disconnectClient(User user)
    {
        disconnectClient(Sessions.getSessionSocket(user));
        Sessions.deleteSession(user);
        ConnectionTimers.removeTimer(user);
    }

    public static void disconnectClient(Socket socket)
    {
        if(socket == null)
            return;

        SelectionKey key = null;

        for(var sock : InputHandler.getSelectionKeys())
            if(((SocketChannel) sock.channel()).socket().equals(socket))
            {
                key = sock;
                break;
            }

        assert key != null;
        try
        {
            key.channel().close();
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        key.cancel();
    }
}
