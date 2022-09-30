package comiam.snakegame.network.messageutills;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.util.Constants;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MessageSender
{
    private static final Logger logger = Logger.getLogger(MessageSender.class.getSimpleName());

    private final DatagramSocket out;
    private final MessageHistory messageHistory;

    private @Nullable Supplier<@Nullable InetSocketAddress> masterAddressSupplier;

    public MessageSender(DatagramSocket out, MessageHistory messageHistory)
    {
        this.out = out;
        this.messageHistory = messageHistory;
    }

    public void send(final AddressedMessage message) throws IOException
    {
        var address = this.getActualDestinationAddress(message);
        if (address == null)
        {
            return;
        }
        if (this.out.getLocalPort() == address.getPort() && isThisMyIpAddress(address.getAddress()))
        {
            logger.info("Message addressed to self, won't send");
            return;
        }
        var contents = message.getMessage();
        var serializedSize = contents.getSerializedSize();
        if (serializedSize > Constants.MAX_PACKET_SIZE_B)
        {
            logger.warning(" Message serialized size is too big (" + serializedSize
                    + " > " + Constants.MAX_PACKET_SIZE_B + "), dropped");
            return;
        }

        var bytes = contents.toByteArray();
        var packet = new DatagramPacket(bytes, 0, bytes.length, address);

        if (contents.hasPing() && !this.messageHistory.isConnectedTo(address))
        {
            logger.info("Destination address " + address + " is unknown for a ping message");
            return;
        }
        if (contents.hasJoin())
        {
            logger.info("Sent a join request to " + address);
        }

        this.out.send(packet);
        message.decrementRetriesCount();
        logger.finest("Sent " + serializedSize + " bytes long packet to " + address);

        if (Constants.announceAddress.equals(address) || contents.hasAck())
        {
            // oh yes, fire & forget, my favourite type of messaging
            return;
        }

        this.messageHistory.messageSent(address, message);
    }

    private synchronized @Nullable InetSocketAddress getActualDestinationAddress(
            final AddressedMessage message)
    {
        if (!message.isAddressedToMaster())
        {
            return message.getAddress();
        }
        if (this.masterAddressSupplier == null)
        {
            logger.warning("Message is for master but no master address supplier provided");
            return null;
        }
        var address = this.masterAddressSupplier.get();
        if (address == null)
        {
            logger.warning("Master address supplier returned null: is this node the master?");
            return null;
        }
        return address;
    }

    public synchronized void setMasterAddressSupplier(
            final Supplier<@Nullable InetSocketAddress> masterAddressSupplier)
    {
        this.masterAddressSupplier = masterAddressSupplier;
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
