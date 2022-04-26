package socksproxy.proxy;

import socksproxy.auth.AuthMode;
import socksproxy.connection.Handler;
import socksproxy.connection.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Set;

public class Proxy implements AutoCloseable, Runnable
{
    private final Selector selector = Selector.open();
    private final Server server;

    public Proxy(int port, HashMap<String, String> users, AuthMode authMode) throws IOException
    {
        server = new Server(port, selector, users, authMode);
    }

    @Override
    public void close() throws Exception
    {
        selector.close();
        server.close();
        server.closeDNS();
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            int count = 0;
            try
            {
                count = selector.select(10000);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            if (count == 0)
                continue;

            Set<SelectionKey> modified = selector.selectedKeys();
            for (SelectionKey selected : modified)
            {
                Handler key = (Handler) selected.attachment();
                key.accept(selected);
            }
            modified.clear();
        }

        try
        {
            close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
