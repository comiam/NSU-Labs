import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main
{
    private static final int                        DEFAULT_PORT            = 3243;
    private static final long                       DISCONNECT_TIMEOUT      = 15000L;
    private static final long                       UPDATE_TIMER            = 1000L;
    private static final String                     DEFAULT_MSG             = "lol_im_here";
    private static final HashMap<String, Long>      clients                 = new HashMap<>();

    private static InetAddress MULTICAST_GROUP;
    private static byte[] recvBuf, sendBuf;

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Invalid args!");
            return;
        }

        try
        {
            MULTICAST_GROUP = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e)
        {
            System.out.println("Wrong host!");
            return;
        }

        long lastPingTime = 0L;

        try(MulticastSocket socket = new MulticastSocket(DEFAULT_PORT))
        {
            ArrayList<String> trashBox = new ArrayList<>();

            MULTICAST_GROUP = InetAddress.getByName(args[0]);
            socket.joinGroup(MULTICAST_GROUP);
            socket.setLoopbackMode(false);
            socket.setSoTimeout(1000);

            System.out.println("Copy started");

            recvBuf = new byte[256];

            while(true)
            {
                long currentTime = System.currentTimeMillis();
                if(currentTime - lastPingTime > UPDATE_TIMER)
                {
                    sendBuf = DEFAULT_MSG.getBytes();
                    socket.send(new DatagramPacket(sendBuf, sendBuf.length, MULTICAST_GROUP, DEFAULT_PORT));
                    lastPingTime = currentTime;
                }

                var pkg = new DatagramPacket(recvBuf, recvBuf.length);
                try
                {
                    socket.receive(pkg);

                    if (clients.containsKey(pkg.getSocketAddress().toString())
                            || pkg.getSocketAddress().toString().contains(Inet.getCurrentIPv4())
                            || pkg.getSocketAddress().toString().contains(Inet.getCurrentIPv6()))
                        continue;

                    System.out.println("New copy from " + pkg.getSocketAddress());

                    clients.put(pkg.getSocketAddress().toString(), System.currentTimeMillis());
                } catch(IOException ignored) {}

                clients.forEach((key, value) -> {
                    if(currentTime - value > DISCONNECT_TIMEOUT)
                    {
                        trashBox.add(key);
                        System.out.println("Copy from " + pkg.getSocketAddress() + " disconnected");
                    }
                });
                for(var s : trashBox)
                    clients.remove(s);
                trashBox.clear();
            }
        } catch(Throwable ex)
        {
            ex.printStackTrace();
        }
    }
}