package socksproxy.connection;

import socksproxy.auth.AuthMode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;

public class Server implements Handler
{

    private final AuthMode authMode;
    private final ServerSocketChannel serverChannel = ServerSocketChannel.open();
    private final DNS dns;
    private final HashMap<String, String> users;

    public Server(int port, Selector selector, HashMap<String, String> users, AuthMode authMode) throws IOException
    {
        this.users = users;
        dns = new DNS(port, selector);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT, this);
        this.authMode = authMode;
    }

    public void closeDNS() throws IOException
    {
        dns.close();
    }

    @Override
    public void close() throws IOException
    {
        serverChannel.close();
    }

    @Override
    public void accept(SelectionKey key)
    {
        try
        {
            if (!key.isValid())
            {
                close();
                return;
            }
            new Connection(serverChannel.accept(), dns, key.selector(), users, authMode);
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }
}
