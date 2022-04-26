package comiam.snakegame.network.messageutills;

import com.google.protobuf.InvalidProtocolBufferException;
import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.util.Constants;
import comiam.snakegame.util.unsafe.UnsafeConsumer;
import comiam.snakegame.util.unsafe.UnsafeRunnable;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.*;
import java.util.Arrays;
import java.util.logging.Logger;

public class MessageReceiver implements UnsafeRunnable
{
    private static final Logger logger = Logger.getLogger(MessageReceiver.class.getSimpleName());

    private final DatagramSocket in;
    private final UnsafeConsumer<AddressedMessage> onReceived;

    private final byte[] buffer = new byte[Constants.MAX_PACKET_SIZE_B];

    public MessageReceiver(DatagramSocket in, UnsafeConsumer<AddressedMessage> onReceived)
    {
        this.in = in;
        this.onReceived = onReceived;
    }

    @Override
    public void run() throws Exception
    {
        while (true)
        {
            var packet = new DatagramPacket(this.buffer, 0, this.buffer.length);

            try
            {
                this.in.receive(packet);
                logger.finest(" Received " + packet.getLength() + " bytes long packet from "
                        + packet.getSocketAddress());
                if (!(packet.getSocketAddress() instanceof InetSocketAddress))
                {
                    logger.info(" Unsupported remote socket address: "
                            + packet.getSocketAddress().getClass().getName());
                    continue;
                }
                if (this.in.getPort() == packet.getPort() && isThisMyIpAddress(packet.getAddress()))
                {
                    logger.info("Received a packet from self, dropping");
                    continue;
                }
                var fromAddress = (InetSocketAddress) packet.getSocketAddress();

                var data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                try
                {
                    var contents = SnakesProto.GameMessage.parseFrom(data);
                    this.onReceived.accept(AddressedMessage.create(fromAddress, contents));
                } catch (final InvalidProtocolBufferException e)
                {
                    logger.info("Received invalid message: " + e.getMessage());
                }
            } catch (final SocketException e)
            {
                logger.info("SocketException: " + e.getMessage());
                return;
            }
        }
    }

    public boolean isThisMyIpAddress(final InetAddress address)
    {

        if (address.isAnyLocalAddress() || address.isLoopbackAddress())
            return true;

        try
        {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (final SocketException e)
        {
            return false;
        }
    }
}
