package comiam.snakegame.network.messageutills;

import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.util.unsafe.UnsafeConsumer;
import org.jetbrains.annotations.Nullable;
import comiam.snakegame.util.Constants;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MessageHistory
{
    private static final Logger logger = Logger.getLogger(MessageHistory.class.getSimpleName());

    private final Map<AddressedMessage, Long> sendTime = new HashMap<>();
    private final Map<AddressedMessage, InetSocketAddress> realDestinationAddress = new HashMap<>();
    private final Map<InetSocketAddress, Long> lastSentToTime = new HashMap<>();
    private final Map<InetSocketAddress, Long> receiveTime = new HashMap<>();
    private final Map<InetSocketAddress, Long> announcementReceiveTime = new HashMap<>();

    public synchronized void messageSent(
            final InetSocketAddress realAddress,
            final AddressedMessage message)
    {
        var time = System.nanoTime() / Constants.MS_PER_NS;
        this.sendTime.put(message, time);
        this.lastSentToTime.put(realAddress, time);
        if (message.isAddressedToMaster())
        {
            this.realDestinationAddress.put(message, realAddress);
        }
        logger.fine("Message " + message.getMessage().getMsgSeq() + " has been sent to " + realAddress);
    }

    public synchronized void deliveryConfirmed(final AddressedMessage acknowledgeMessage)
    {
        this.sendTime.remove(acknowledgeMessage); // equals and hashCode use sequence number
        logger.fine("Message " + acknowledgeMessage.getMessage().getMsgSeq() + " delivery confirmed");
    }

    public synchronized void forEachNotAcknowledged(
            final int timeout,
            final UnsafeConsumer<AddressedMessage> action) throws Exception
    {
        var now = System.nanoTime() / Constants.MS_PER_NS;
        for (final var entry : this.sendTime.entrySet())
        {
            var message = entry.getKey();
            var sendTime = entry.getValue();

            if (now - sendTime > timeout)
            {
                action.accept(message);
            }
        }
    }

    public synchronized void forEachNotContacted(
            final int timeout,
            final UnsafeConsumer<InetSocketAddress> action) throws Exception
    {
        var now = System.nanoTime() / Constants.MS_PER_NS;
        for (final var entry : this.lastSentToTime.entrySet())
        {
            var address = entry.getKey();
            var sendTime = entry.getValue();

            if (now - sendTime > timeout)
            {
                action.accept(address);
            }
        }
    }

    public synchronized void messageReceived(final InetSocketAddress fromAddress)
    {
        var time = System.nanoTime() / Constants.MS_PER_NS;
        this.receiveTime.put(fromAddress, time);
        logger.fine("Received message from " + fromAddress);
    }

    public synchronized void announcementReceived(final InetSocketAddress fromAddress)
    {
        var time = System.nanoTime() / Constants.MS_PER_NS;
        this.announcementReceiveTime.put(fromAddress, time);
    }

    public synchronized void forEachOldAnnouncement(
            final int timeout,
            final UnsafeConsumer<InetSocketAddress> action) throws Exception
    {
        var now = System.nanoTime() / Constants.MS_PER_NS;
        for (final var entry : this.announcementReceiveTime.entrySet())
        {
            var message = entry.getKey();
            var receiveTime = entry.getValue();

            if (now - receiveTime > timeout)
            {
                action.accept(message);
            }
        }
    }

    public synchronized void forEachDisconnected(
            final int timeout,
            final UnsafeConsumer<InetSocketAddress> action) throws Exception
    {
        var now = System.nanoTime() / Constants.MS_PER_NS;
        for (final var entry : this.receiveTime.entrySet())
        {
            var address = entry.getKey();
            var receiveTime = entry.getValue();

            if (now - receiveTime > timeout)
            {
                action.accept(address);
            }
        }
    }

    public synchronized void removeAnnouncementRecord(final InetSocketAddress fromAddress)
    {
        var removed = this.announcementReceiveTime.remove(fromAddress);
        logger.log((removed == null ? Level.WARNING : Level.FINE),
                "Record about announcement received from " + fromAddress
                        + (removed == null ? " hasn't been removed" : " has been removed"));
    }

    public synchronized void removeConnectionRecord(final InetSocketAddress address)
    {
        var removed = this.receiveTime.remove(address);
        logger.log((removed == null ? Level.WARNING : Level.FINE),
                "Node " + address
                        + (removed == null ? " hasn't been removed" : " has been removed"));
        removed = this.lastSentToTime.remove(address);
        logger.log((removed == null ? Level.WARNING : Level.FINE),
                "Last send time for node " + address
                        + (removed == null ? " hasn't been removed" : " has been removed"));
        var messagesRemoved = this.sendTime.keySet().removeIf(
                message -> !message.isAddressedToMaster() && address.equals(message.getAddress())
                        || message.isAddressedToMaster() && address.equals(this.realDestinationAddress.get(message)));
        logger.fine(
                messagesRemoved
                        ? "Messages to " + address + " have been removed"
                        : "No messages were addressed to " + address);
        this.realDestinationAddress.keySet().removeIf(it -> this.realDestinationAddress.get(it).equals(address));
    }

    /**
     * Checks whether this node is connected to node with address {@code address}
     *
     * @param address address of the other node
     * @return {@code true} if and only if this node received anything from the other node within timeout
     */
    public synchronized boolean isConnectedTo(final InetSocketAddress address)
    {
        return this.receiveTime.containsKey(address);
    }

    public synchronized @Nullable InetSocketAddress getRealDestinationAddress(final AddressedMessage message)
    {
        return this.realDestinationAddress.get(message);
    }
}
