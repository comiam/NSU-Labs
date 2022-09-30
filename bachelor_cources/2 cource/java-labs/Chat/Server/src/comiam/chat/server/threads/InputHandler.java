package comiam.chat.server.threads;

import comiam.chat.server.connection.Connection;
import comiam.chat.server.core.ServerCore;
import comiam.chat.server.logger.Log;
import comiam.chat.server.utils.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import static comiam.chat.server.core.GlobalConstants.MAX_MESSAGE_SIZE;

public class InputHandler implements Runnable
{
    private final int port;
    private static Selector selector;

    private static final Object locker = new Object();

    public InputHandler(int port)
    {
        this.port = port;
    }

    public static void wakeUpSamurai()
    {
        synchronized(locker)
        {
            selector.wakeup();
        }
    }

    public static Set<SelectionKey> getSelectionKeys()
    {
        synchronized(locker)
        {
            return selector.keys();
        }
    }

    public static void closeChannelKey(SelectionKey key, Socket socket)
    {
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

    @Override
    public void run()
    {
        ServerSocket serverSocket;

        synchronized(locker)
        {
            try
            {
                var pair = openSelector(port);
                selector = pair.getFirst();
                serverSocket = pair.getSecond();
            } catch(IOException e)
            {
                Log.error("Input Thread: Can't open selector! Server shutting down...");
                ServerCore.shutdown();
                return;
            }
        }

        SelectionKey selectionKey = null;

        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                int count = selector.select();

                if(Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        selector.close();
                    }catch(Throwable ignored){}
                    return;
                }

                if(count == 0)
                    continue;

                synchronized(locker)
                {
                    Set<?> key = selector.selectedKeys();
                    Iterator<?> it = key.iterator();

                    while(it.hasNext())
                    {
                        selectionKey = (SelectionKey) it.next();
                        it.remove();

                        if(selectionKey.isAcceptable())
                            Connection.acceptConnection(selector, serverSocket, selectionKey);
                        else if(selectionKey.isReadable())
                            readInputMessage(selectionKey);
                    }
                }
            } catch(Throwable e)
            {
                Log.error("Input Thread: Error while receiving message...", e);

                if(selectionKey != null)
                    selectionKey.cancel();
            }
        }
    }

    private static Pair<Selector, ServerSocket> openSelector(int port) throws IOException
    {
        Selector selector;
        ServerSocket serverSocket;

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        return new Pair<>(selector, serverSocket);
    }

    private static void readInputMessage(SelectionKey selectionKey)
    {
        StringBuilder tmpMessage = new StringBuilder();
        ByteBuffer sharedBuffer = ByteBuffer.allocateDirect(2048);
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        int size;

        String address = socketChannel.socket().getInetAddress().toString();
        Log.info("Input Thread: Receiving a message by " + address);

        try
        {
            if(socketChannel.read(sharedBuffer) < 4)
            {
                Log.error("Input Thread: The header size of the received message by " + address + " is too small. Aborting...");
                tmpMessage.setLength(0);
                sharedBuffer.clear();

                selectionKey.cancel();

                return;
            }
            sharedBuffer.flip();

            size = sharedBuffer.getInt();

            if(size <= 0 || size >= MAX_MESSAGE_SIZE)
            {
                Log.error("Input Thread: Invalid size of the received message by " + address + ". Aborting...");
                tmpMessage.setLength(0);
                sharedBuffer.clear();

                selectionKey.cancel();

                return;
            }

            tmpMessage.append(StandardCharsets.UTF_8.decode(sharedBuffer.limit(size + 4)).toString());

            if(tmpMessage.toString().isEmpty())
                Log.error("Input Thread: Received message by " + address + " is empty!");
            else
            {
                MessageHandler.addNewMessage(socketChannel.socket(), tmpMessage.toString());
                Log.info("Input Thread: Message by " + address + " received successfully!");
            }
        } catch(IOException e)
        {
            Log.error("Input Thread: Error while receiving message by " + address, e);
            selectionKey.cancel();
        }

        sharedBuffer.clear();
    }
}
