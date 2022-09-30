package socksproxy.connection;

import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DNS implements SocketHandler
{
    private final DatagramChannel resolverChannel = DatagramChannel.open();
    private final InetSocketAddress DnsServerAddr;

    private final ByteBuffer readBuff = ByteBuffer.allocateDirect(Message.MAXLENGTH);
    private final ByteBuffer writeBuff = ByteBuffer.allocateDirect(Message.MAXLENGTH);

    private final SelectionKey key;

    private final Deque<Message> deque = new LinkedList<>();

    private final Map<Integer, Connection> attachments = new HashMap<>();

    public DNS(int port, Selector selector) throws IOException
    {
        resolverChannel.configureBlocking(false);
        resolverChannel.register(selector, 0, this);
        key = resolverChannel.keyFor(selector);
        resolverChannel.bind(new InetSocketAddress(port));
        DnsServerAddr = ResolverConfig.getCurrentConfig().server();
        resolverChannel.connect(DnsServerAddr);
        readBuff.clear();
        writeBuff.clear();

    }

    public void sendToResolve(String domainName, Connection handler)
    {
        try
        {
            Message dnsRequest = Message.newQuery(Record.newRecord(new Name(domainName + '.'), Type.A, DClass.IN));
            deque.addLast(dnsRequest);
            attachments.put(dnsRequest.getHeader().getID(), handler);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } catch (TextParseException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException
    {
        resolverChannel.close();
    }

    @Override
    public void accept(SelectionKey key)
    {
        try
        {
            if (!key.isValid())
            {
                this.close();
                key.cancel();
                return;
            }
            if (key.isReadable())
                read(key);
            else if (key.isWritable())
                write(key);
        } catch (IOException ex)
        {
            ex.printStackTrace();
            try
            {
                this.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void read(SelectionKey key) throws IOException
    {
        if (resolverChannel.receive(readBuff) != null)
        {
            readBuff.flip();
            byte[] data = new byte[readBuff.limit()];
            readBuff.get(data);
            readBuff.clear();
            Message response = new Message(data);
            Connection session = attachments.remove(response.getHeader().getID());
            for (Record record : response.getSection(Section.ANSWER))
                if (record instanceof ARecord)
                {
                    ARecord it = (ARecord) record;
                    if (session.connectToServer(it.getAddress()))
                        break;
                }
        }
        if (attachments.isEmpty())
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
    }

    @Override
    public void write(SelectionKey key) throws IOException
    {
        Message dnsRequest = deque.pollFirst();
        while (dnsRequest != null)
        {
            writeBuff.clear();
            writeBuff.put(dnsRequest.toWire());
            writeBuff.flip();
            if (resolverChannel.send(writeBuff, DnsServerAddr) == 0)
            {
                deque.addFirst(dnsRequest);
                break;
            }
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            dnsRequest = deque.pollFirst();
        }
        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
    }
}

