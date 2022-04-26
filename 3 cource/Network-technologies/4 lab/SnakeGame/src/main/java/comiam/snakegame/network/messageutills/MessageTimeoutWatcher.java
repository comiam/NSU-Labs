package comiam.snakegame.network.messageutills;

import comiam.snakegame.gamelogic.event.EventChannel;
import comiam.snakegame.gamelogic.event.events.AnnouncementTimedOut;
import comiam.snakegame.gamelogic.event.events.NodeTimedOut;
import comiam.snakegame.gamelogic.event.events.NotAcknowledged;
import comiam.snakegame.gamelogic.event.events.PingTimer;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.logging.Logger;

public final class MessageTimeoutWatcher
{
    private static final Logger logger = Logger.getLogger(MessageTimeoutWatcher.class.getSimpleName());

    private final EventChannel out;
    private final MessageHistory messageHistory;

    private final int ackTimeout;
    private final int pingTimeout;
    private final int nodeTimeout;
    private final int announcementTimeout;

    public MessageTimeoutWatcher(EventChannel out, MessageHistory messageHistory, int ackTimeout, int pingTimeout, int nodeTimeout, int announcementTimeout)
    {
        this.out = out;
        this.messageHistory = messageHistory;
        this.ackTimeout = ackTimeout;
        this.pingTimeout = pingTimeout;
        this.nodeTimeout = nodeTimeout;
        this.announcementTimeout = announcementTimeout;
    }

    public void handleTimeouts() throws Exception
    {
        synchronized (this.messageHistory)
        {
            var toRemove = new HashSet<InetSocketAddress>();

            logger.finest("Checking last received time");
            this.messageHistory.forEachDisconnected(this.nodeTimeout, address -> {
                logger.fine("Haven't received anything from " + address + " for longer than timeout");
                toRemove.add(address);
                this.out.submit(new NodeTimedOut(address));
            });

            toRemove.forEach(this.messageHistory::removeConnectionRecord);
            toRemove.clear();

            logger.finest("Checking sent messages");
            this.messageHistory.forEachNotAcknowledged(this.ackTimeout, message -> {
                logger.finest(
                        "Message " + message.getMessage().getMsgSeq() + " addressed to "
                                + (message.isAddressedToMaster() ? "master" : message.getAddress())
                                + " not acknowledged within timeout");
                var toAddress = message.isAddressedToMaster()
                        ? this.messageHistory.getRealDestinationAddress(message)
                        : message.getAddress();
                if (toAddress == null)
                {
                    logger.warning("No real destination address for message " + message.getMessage().getMsgSeq());
                } else
                {
                    // For special cases when this node started sending messages to another node
                    // without knowing anything about it (i.e. if it's even online)
                    if (!message.retriesLeft() && !this.messageHistory.isConnectedTo(toAddress))
                    {
                        // If a message has no retries left and no messages were ever received from it's destination
                        // generate a NodeTimedOut event to tell whoever who's been sending the message to stop
                        // sending anything to this destination
                        this.out.submit(new NodeTimedOut(toAddress));
                        toRemove.add(toAddress);
                        logger.info("Submitted NodeTimedOut event");
                    }
                }
                if (message.retriesLeft())
                {
                    this.out.submit(new NotAcknowledged(message));
                }
            });

            toRemove.forEach(this.messageHistory::removeConnectionRecord);

            logger.finest("Checking last contacted to time");
            this.messageHistory.forEachNotContacted(this.pingTimeout, address -> {
                logger.fine("Haven't sent anything to " + address + " for longer than timeout");
                this.out.submit(new PingTimer(address));
            });

            logger.finest("Checking announcements receive time");
            this.messageHistory.forEachOldAnnouncement(this.announcementTimeout, address -> {
                logger.finest("Announcement form " + address + " timed out");
                this.out.submit(new AnnouncementTimedOut(address));
            });
        }
    }
}
