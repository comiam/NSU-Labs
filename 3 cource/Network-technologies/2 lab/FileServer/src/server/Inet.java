package server;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class Inet
{
    private static String currentIPv4;
    private static String currentIPv6;

    public static String getCurrentIPv4()
    {
        return currentIPv4;
    }

    public static String getCurrentIPv6()
    {
        return currentIPv6;
    }

    static
    {
        try (final DatagramSocket socket = new DatagramSocket())
        {
            socket.connect(Inet6Address.getByName("8.8.8.8"), 10002);
            currentIPv4 = socket.getLocalAddress().getHostAddress();
            currentIPv6 = findIPv6(currentIPv4);
        } catch (SocketException | UnknownHostException e)
        {
            System.out.println("Lol, i have troubles");
            System.exit(-1);
        }
    }

    private static String findIPv6(String ipv4Addr)
    {
        Enumeration<NetworkInterface> nets;
        try
        {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e)
        {
            return null;
        }

        for (NetworkInterface netint : Collections.list(nets))
        {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();

            boolean contains = false;
            for(InetAddress inetAddress : Collections.list(inetAddresses))
                if(inetAddress.toString().contains(ipv4Addr))
                {
                    contains = true;
                    break;
                }


            if(contains)
            {
                inetAddresses = netint.getInetAddresses();
                for(InetAddress inetAddress : Collections.list(inetAddresses))
                    if(inetAddress instanceof Inet6Address)
                        return inetAddress.toString().split("%")[0];
            }
        }
        return null;
    }
}
