package comiam.snakegame;

import comiam.snakegame.network.messageutills.MessageHistory;
import comiam.snakegame.network.messageutills.MessageReceiver;
import comiam.snakegame.network.messageutills.MessageSender;
import comiam.snakegame.network.messageutills.MessageTimeoutWatcher;
import comiam.snakegame.network.Node;
import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.event.EventQueueProcessor;
import comiam.snakegame.gamelogic.event.events.*;
import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gamelogic.gameobjects.config.InvalidConfigException;
import comiam.snakegame.gui.menu.MenuWindow;
import comiam.snakegame.gui.util.GuiUtils;
import comiam.snakegame.util.Constants;
import comiam.snakegame.util.unsafe.UnsafeRunnable;
import comiam.snakegame.util.LoggedTimer;
import comiam.snakegame.util.Scheduler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.HashSet;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class Main
{
    private static final String LOGGING_PROPERTIES_FILE = "/logging.properties";
    private static final Logger logger;

    static
    {
        tryInitLogger();
        logger = Logger.getLogger(Main.class.getSimpleName());
    }

    private Main()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void main(final String[] args)
    {

        GuiUtils.setUIComponentsColors();

        var cfg = Config.DEFAULT_CONFIG;
        try
        {
            cfg = Config.load();
        } catch (final InvalidConfigException e)
        {
            logger.warning("Invalid config: " + e.getMessage() + ", will use default instead");
        }
        var config = cfg;

        var eventProcessor = new EventQueueProcessor();
        var eventProcessorThread = createEventProcessorDaemon(eventProcessor);

        var messageHistory = new MessageHistory();

        eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasAck(),
                event -> messageHistory.deliveryConfirmed(event.<IncomingMessage>get().message));

        var announcements = new HashSet<AddressedMessage>();

        try (
                var multicastReceiverSocket = new MulticastSocket(Constants.MULTICAST_PORT);
                var generalPurposeSocket = new MulticastSocket(Integer.parseInt(args[0])))
        {
            multicastReceiverSocket.joinGroup(Constants.announceAddress, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));


            logger.info("Running on " + generalPurposeSocket.getLocalSocketAddress());

            var sender = new MessageSender(generalPurposeSocket, messageHistory);

            eventProcessor.addHandler(
                    event -> event instanceof OutgoingMessage,
                    event -> sender.send(event.<OutgoingMessage>get().message));

            eventProcessor.addHandler(
                    event -> event instanceof NotAcknowledged,
                    event -> eventProcessor.submit(new OutgoingMessage(event.<NotAcknowledged>get().message)));

            eventProcessor.addHandler(
                    event -> event instanceof NodeTimedOut,
                    event -> messageHistory.removeConnectionRecord(event.<NodeTimedOut>get().nodeAddress));

            var unicastReceiver = new MessageReceiver(generalPurposeSocket, message -> {
                messageHistory.messageReceived(message.getAddress());
                eventProcessor.submit(new IncomingMessage(message));
            });

            var unicastReceiverThread = createUnicastReceiverDaemon(unicastReceiver);
            unicastReceiverThread.start();

            var multicastReceiver = new MessageReceiver(multicastReceiverSocket, message -> {
                messageHistory.announcementReceived(message.getAddress());
                eventProcessor.submit(new Announcement(message));
            });

            var timer = new LoggedTimer();
            var scheduler = Scheduler.fromTimer(timer);

            var timeoutManager = new MessageTimeoutWatcher(
                    eventProcessor, messageHistory, config.getPingDelayMs(),
                    config.getPingDelayMs(), config.getNodeTimeoutMs(),
                    Constants.ANNOUNCE_DELAY_MS * 3 / 2);
            var handleTimeoutsTask = scheduler.schedule(
                    timeoutManager::handleTimeouts, (config.getPingDelayMs() + 1) / 2);
            eventProcessorThread.start();

            var menuWindow = new MenuWindow(
                    "Snakes", config, announcements,
                    (name, baseConfig, view) ->
                            Node.createHost(name, baseConfig, view, scheduler, eventProcessor, eventProcessor),
                    (name, baseConfig, host, view, onSuccess, onError) -> {
                        try
                        {
                            Node.createClient(
                                    name, baseConfig, host, view, scheduler, eventProcessor,
                                    eventProcessor, sender::setMasterAddressSupplier, onSuccess, onError);
                        } catch (final InterruptedException unused)
                        {
                            logger.info("Interrupted when connecting to " + host);
                        }
                    });
            menuWindow.getExitHookRegisterer().accept(handleTimeoutsTask::cancel);
            menuWindow.getExitHookRegisterer().accept(timer::cancel);
            menuWindow.getExitHookRegisterer().accept(generalPurposeSocket::close);
            menuWindow.getExitHookRegisterer().accept(multicastReceiverSocket::close);
            menuWindow.makeVisible();

            var runningGamesView = menuWindow.getRunningGamesView();

            eventProcessor.addHandler(
                    event -> event instanceof Announcement,
                    event -> {
                        synchronized (announcements)
                        {
                            var message = event.<Announcement>get().message;
                            announcements.removeIf(it -> it.getAddress().equals(message.getAddress()));
                            announcements.add(message);
                        }
                        runningGamesView.updateView();
                    });
            eventProcessor.addHandler(
                    event -> event instanceof AnnouncementTimedOut,
                    event -> {
                        var fromAddress = event.<AnnouncementTimedOut>get().fromAddress;
                        synchronized (announcements)
                        {
                            announcements.removeIf(it -> it.getAddress().equals(fromAddress));
                        }
                        messageHistory.removeAnnouncementRecord(fromAddress);
                        runningGamesView.updateView();
                    });

            multicastReceiver.run();
        } catch (final InterruptedException e)
        {
            logger.info(Thread.currentThread().getName() + " interrupted");
        } catch (final Exception e)
        {
            logger.severe(e.getMessage());
        }
    }

    private static void tryInitLogger()
    {
        try
        {
            final var config = Main.class.getResourceAsStream(LOGGING_PROPERTIES_FILE);
            if (config == null)
            {
                throw new IOException("Cannot load \"" + LOGGING_PROPERTIES_FILE + "\"");
            }
            LogManager.getLogManager().readConfiguration(config);
        } catch (final IOException e)
        {
            System.err.println("Could not setup logger configuration: " + e.getMessage());
        }
    }

    private static Thread createUnicastReceiverDaemon(
            final UnsafeRunnable task)
    {
        var thread = new Thread(() -> {
            try
            {
                task.run();
            } catch (final Exception e)
            {
                logger.info(Thread.currentThread().getName() + ": " + e.getMessage());
            }
        });
        thread.setName("Unicast-Receiver-Thread");
        thread.setDaemon(true);
        return thread;
    }

    private static Thread createEventProcessorDaemon(
            final Runnable task)
    {
        var thread = new Thread(task);
        thread.setName("Event-Processor-Thread");
        thread.setDaemon(true);
        return thread;
    }
}