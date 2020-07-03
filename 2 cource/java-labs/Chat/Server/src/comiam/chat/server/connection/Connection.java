package comiam.chat.server.connection;

import comiam.chat.server.data.session.Sessions;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.threads.InputHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Connection
{
    public static void acceptConnection(Selector selector, ServerSocket serverSocket, SelectionKey selectionKey)
    {
        Socket socket;
        SocketChannel channel;
        try
        {
            socket = serverSocket.accept();
            Log.info("Input Thread: Get connection from: " + socket.getInetAddress());
            channel = socket.getChannel();
        } catch(IOException e)
        {
            Log.error("Input Thread: Unable to accept connection!", e);
            try
            {
                selectionKey.channel().close();
            } catch(IOException ex)
            {
                ex.printStackTrace();
            }
            selectionKey.cancel();
            return;
        }

        if(channel != null)
        {
            try
            {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
            } catch(IOException e)
            {
                Log.error("Input Thread: Unable to use new channel!", e);
                try
                {
                    socket.close();
                } catch(IOException ex)
                {
                    ex.printStackTrace();
                }
                selectionKey.cancel();
                return;
            }
        }
        Log.info("Input Thread: Connection with " + socket.getInetAddress() + " is established!");
        ConnectionTimers.addAuthTimer(socket);
    }

    public static void disconnectClient(User user)
    {
        disconnectClient(Sessions.getSession(user).getConnection());
        Sessions.deleteSession(user);
    }

    public static void disconnectClient(Socket socket)
    {
        if(socket == null)
            return;

        SelectionKey key = null;

        for(var sock : InputHandler.getSelectionKeys())
            if(sock.channel() instanceof SocketChannel)
                if(((SocketChannel) sock.channel()).socket().equals(socket))
                {
                    key = sock;
                    break;
                }

        assert key != null;
        InputHandler.closeChannelKey(key, socket);
    }
}
