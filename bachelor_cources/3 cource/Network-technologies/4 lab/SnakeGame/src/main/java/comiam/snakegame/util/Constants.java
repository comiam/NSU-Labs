package comiam.snakegame.util;

import java.net.InetSocketAddress;

public final class Constants
{
    public static final int MS_PER_NS = 1_000_000;

    public static final int MULTICAST_PORT = 9192;
    public static final String MULTICAST_GROUP = "239.192.0.4";

    public static final InetSocketAddress announceAddress
            = new InetSocketAddress(MULTICAST_GROUP, MULTICAST_PORT);

    public static final int ANNOUNCE_DELAY_MS = 1_000;

    public static final int MAX_NAME_LENGTH = 13;

    public static final int MAX_PACKET_SIZE_B = 65_000;

    private Constants()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
