package comiam.snakegame.network;

import comiam.snakegame.network.message.MessageFactory;
import comiam.snakegame.gamelogic.GameModel;
import comiam.snakegame.gamelogic.gamefield.Direction;
import comiam.snakegame.gamelogic.snake.Steerable;
import comiam.snakegame.gui.util.Colours;
import comiam.snakegame.gamelogic.gameobjects.config.Config;
import comiam.snakegame.gamelogic.gameobjects.game.GameState;
import comiam.snakegame.gamelogic.gameobjects.player.Player;
import comiam.snakegame.gamelogic.event.EventChannel;
import comiam.snakegame.gamelogic.event.EventProcessor;
import comiam.snakegame.gamelogic.event.HandlerDescriptor;
import comiam.snakegame.gamelogic.event.events.IncomingMessage;
import comiam.snakegame.gamelogic.event.events.NodeTimedOut;
import comiam.snakegame.gamelogic.event.events.OutgoingMessage;
import comiam.snakegame.gamelogic.event.events.PingTimer;
import comiam.snakegame.gui.game.SnakesGameView;
import comiam.snakegame.network.message.AddressedMessage;
import comiam.snakegame.gamelogic.snake.impl.LocalSnake;
import comiam.snakegame.util.Constants;
import comiam.snakegame.util.Scheduler;
import me.ippolitov.fit.snakes.SnakesProto;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static comiam.snakegame.util.Constants.ANNOUNCE_DELAY_MS;
import static comiam.snakegame.util.Constants.announceAddress;

public final class Node
{
    private static final int DEFAULT_OWN_ID = 0;

    private static final Logger logger = Logger.getLogger(Node.class.getSimpleName());

    private SnakesProto.NodeRole role;
    private final EventChannel eventChannel;
    private final EventProcessor eventProcessor;

    private final Map<InetSocketAddress, Integer> playerIdByAddress = new HashMap<>();
    private final Map<Integer, Steerable> steerableByPlayerId = new HashMap<>();
    private final Map<Integer, Long> latestSteerSeq = new HashMap<>();

    private final int id;
    private int maxId = DEFAULT_OWN_ID;
    private int masterId;

    private @Nullable InetSocketAddress masterAddress;
    private @Nullable InetSocketAddress deputyAddress;

    private final GameModel gameModel;
    private final SnakesGameView view;

    private final Scheduler scheduler;
    private final int stateDelayMs;

    private final @Nullable HandlerDescriptor stateHandler;

    private final Object lock;

    private Node(
            final String userName,
            final Config config,
            final SnakesGameView view,
            final Scheduler scheduler,
            final EventProcessor eventProcessor,
            final EventChannel eventChannel)
    {
        this.role = SnakesProto.NodeRole.MASTER;
        this.eventChannel = eventChannel;
        this.eventProcessor = eventProcessor;
        this.id = DEFAULT_OWN_ID;
        this.masterId = DEFAULT_OWN_ID;
        this.view = view;
        this.scheduler = scheduler;
        this.stateDelayMs = config.getStateDelayMs();

        var self = new Player(
                userName, this.id, "", 0, this.role, SnakesProto.PlayerType.HUMAN, 0);
        this.gameModel = new GameModel(config);
        this.lock = this.gameModel;
        var ownSnake = this.gameModel.addPlayer(self, LocalSnake::new);
        if (ownSnake == null)
        {
            throw new IllegalStateException("Cannot create a single snake");
        }
        this.steerableByPlayerId.put(this.id, ownSnake);
        this.bindControlsToLocal();
        view.bindTo(this.gameModel);
        view.setPreferredColour(this.id, Colours.GREEN);

        this.stateHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasState(),
                event -> this.handleStateMessage(event.<IncomingMessage>get().message));
        view.getExitHookRegisterer().accept(this.stateHandler::remove);

        var joinHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasJoin(),
                event -> this.handleJoinMessage(event.<IncomingMessage>get().message));
        this.view.getExitHookRegisterer().accept(joinHandler::remove);
        this.view.getLeaveHookRegisterer().accept(joinHandler::remove);

        var steerHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasSteer(),
                event -> this.handleSteerMessage(event.<IncomingMessage>get().message));
        this.view.getExitHookRegisterer().accept(steerHandler::remove);

        var disconnectHandler = eventProcessor.addHandler(
                event -> event instanceof NodeTimedOut,
                event -> this.onPlayerDisconnected(event.<NodeTimedOut>get().nodeAddress));
        this.view.getExitHookRegisterer().accept(disconnectHandler::remove);

        var roleChangeHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasRoleChange(),
                event -> this.handleRoleChangeMessage(event.<IncomingMessage>get().message));
        this.view.getExitHookRegisterer().accept(roleChangeHandler::remove);

        var pingSubmitter = eventProcessor.addHandler(
                event -> event instanceof PingTimer,
                event -> eventChannel.submit(
                        new OutgoingMessage(
                                AddressedMessage.create(
                                        event.<PingTimer>get().who, MessageFactory.createPingMessage()))));
        this.view.getExitHookRegisterer().accept(pingSubmitter::remove);

        var pingHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasPing(),
                event -> this.handlePingMessage(event.<IncomingMessage>get().message));
        this.view.getExitHookRegisterer().accept(pingHandler::remove);
    }

    public static void createHost(
            final String userName,
            final Config config,
            final SnakesGameView view,
            final Scheduler scheduler,
            final EventProcessor eventProcessor,
            final EventChannel eventChannel)
    {
        var node = new Node(userName, config, view, scheduler, eventProcessor, eventChannel);

        var stateTask = node.scheduler.schedule(node::nextState, node.stateDelayMs);
        var announceTask = node.scheduler.schedule(node::submitAnnouncement, ANNOUNCE_DELAY_MS);

        node.view.getExitHookRegisterer().accept(stateTask::cancel);
        node.view.getExitHookRegisterer().accept(announceTask::cancel);
        node.view.getExitHookRegisterer().accept(() -> {
            node.role = SnakesProto.NodeRole.VIEWER;
            synchronized (node.lock)
            {
                if (node.deputyAddress != null && node.role == SnakesProto.NodeRole.MASTER)
                {
                    try
                    {
                        node.submitRoleChangeMessage(node.deputyAddress, SnakesProto.NodeRole.MASTER);
                    } catch (final InterruptedException e)
                    {
                        logger.info(
                                Thread.currentThread().getName() + " interrupted when submitting role change " +
                                        "message before exiting");
                    }
                }
            }
        });

        node.view.getLeaveHookRegisterer().accept(() -> {
            synchronized (node.gameModel)
            {
                var pastRole = node.role;
                node.role = SnakesProto.NodeRole.VIEWER;
                stateTask.cancel();
                announceTask.cancel();
                node.gameModel.playerLeft(node.id);
                node.view.updateView();
                if (pastRole == SnakesProto.NodeRole.MASTER && node.deputyAddress != null)
                {
                    node.masterAddress = node.deputyAddress;
                    node.deputyAddress = null;
                    try
                    {
                        node.submitRoleChangeMessage(node.masterAddress, SnakesProto.NodeRole.MASTER);
                        node.bindControlsToRemote();
                    } catch (final InterruptedException e)
                    {
                        logger.info(
                                Thread.currentThread().getName() + " interrupted when submitting role change " +
                                        "message after leaving");
                    }
                }
            }
        });

        view.makeVisible();
    }

    private Node(
            final SnakesProto.NodeRole role,
            final int id,
            final int masterId,
            final InetSocketAddress masterAddress,
            final Config config,
            final SnakesGameView view,
            final Scheduler scheduler,
            final EventProcessor eventProcessor,
            final EventChannel eventChannel)
    {
        this.role = role;
        this.eventChannel = eventChannel;
        this.eventProcessor = eventProcessor;
        this.id = id;
        this.masterId = masterId;
        this.masterAddress = masterAddress;
        this.view = view;
        this.scheduler = scheduler;
        this.stateDelayMs = config.getStateDelayMs();

        this.gameModel = new GameModel(config);
        this.lock = this.gameModel;

        this.bindControlsToRemote();
        view.bindTo(this.gameModel);
        view.setPreferredColour(this.id, Colours.GREEN);

        this.stateHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasState(),
                event -> this.handleStateMessage(event.<IncomingMessage>get().message));
        view.getExitHookRegisterer().accept(this.stateHandler::remove);

        var disconnectHandler = eventProcessor.addHandler(
                event -> event instanceof NodeTimedOut,
                event -> this.onPlayerDisconnected(event.<NodeTimedOut>get().nodeAddress));
        view.getExitHookRegisterer().accept(disconnectHandler::remove);

        var roleChangeHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasRoleChange(),
                event -> this.handleRoleChangeMessage(event.<IncomingMessage>get().message));
        view.getExitHookRegisterer().accept(roleChangeHandler::remove);

        var pingSubmitter = eventProcessor.addHandler(
                event -> event instanceof PingTimer,
                event -> eventChannel.submit(
                        new OutgoingMessage(
                                AddressedMessage.create(
                                        event.<PingTimer>get().who, MessageFactory.createPingMessage()))));
        this.view.getExitHookRegisterer().accept(pingSubmitter::remove);

        var pingHandler = eventProcessor.addHandler(
                event -> event instanceof IncomingMessage
                        && event.<IncomingMessage>get().message.getMessage().hasPing(),
                event -> this.handlePingMessage(event.<IncomingMessage>get().message));
        this.view.getExitHookRegisterer().accept(pingHandler::remove);
    }

    private static void createClient(
            final SnakesProto.NodeRole role,
            final String userName,
            final Config config,
            final InetSocketAddress hostAddress,
            final SnakesGameView view,
            final Scheduler scheduler,
            final EventProcessor eventProcessor,
            final EventChannel eventChannel,
            final Consumer<Supplier<InetSocketAddress>> masterAddressSupplierConsumer,
            // i'm sorry
            final Runnable onSuccess,
            final Consumer<String> onError) throws InterruptedException
    {
        var gameModel = new GameModel(config);
        view.bindTo(gameModel);

        final Consumer<SnakesProto.GameMessage> onRejected = message -> {
            var errorMessage = "Cannot join:\n" + message.getError().getErrorMessage();
            logger.info(errorMessage);
            onError.accept(errorMessage);
        };
        final Consumer<SnakesProto.GameMessage> onAcknowledged = message -> {
            var masterId = message.getSenderId();
            var id = message.getReceiverId();
            var node = new Node(
                    role, id, masterId, hostAddress, config, view, scheduler, eventProcessor, eventChannel);
            node.playerIdByAddress.put(hostAddress, masterId);
            masterAddressSupplierConsumer.accept(node::getMasterAddress);

            view.getLeaveHookRegisterer().accept(() -> {
                synchronized (node.lock)
                {
                    node.role = SnakesProto.NodeRole.VIEWER;
                    if (node.masterAddress != null)
                    {
                        try
                        {
                            node.submitRoleChangeMessage(node.masterAddress);
                        } catch (final InterruptedException e)
                        {
                            logger.info(
                                    Thread.currentThread().getName() + " interrupted when submitting role change " +
                                            "message before exiting");
                        }
                    }
                }
            });
            view.makeVisible();
            onSuccess.run();
        };

        var noConnectionHandler = eventProcessor.addOneOffHandler(
                event -> event instanceof NodeTimedOut
                        && event.<NodeTimedOut>get().nodeAddress.equals(hostAddress),
                event -> {
                    var msg = "No response from " + hostAddress;
                    logger.info(msg);
                    onError.accept(msg);
                });

        eventProcessor.addOneOffHandler(
                event -> event instanceof IncomingMessage
                        && (event.<IncomingMessage>get().message.getMessage().hasAck()
                        || event.<IncomingMessage>get().message.getMessage().hasError()),
                event -> {
                    noConnectionHandler.remove();

                    var message = event.<IncomingMessage>get().message.getMessage();
                    if (message.hasAck())
                    {
                        onAcknowledged.accept(message);
                    } else if (message.hasError())
                    {
                        onRejected.accept(message);
                    }
                });

        eventChannel.submit(
                new OutgoingMessage(
                        AddressedMessage.create(
                                hostAddress,
                                MessageFactory.createJoinMessage(
                                        userName, false, role == SnakesProto.NodeRole.VIEWER))));
    }

    public static void createClient(
            final String userName,
            final Config config,
            final InetSocketAddress hostAddress,
            final SnakesGameView view,
            final Scheduler scheduler,
            final EventProcessor eventProcessor,
            final EventChannel eventChannel,
            final Consumer<Supplier<InetSocketAddress>> masterAddressSupplierConsumer,
            final Runnable onSuccess,
            final Consumer<String> onError) throws InterruptedException
    {
        createClient(
                SnakesProto.NodeRole.NORMAL, userName, config, hostAddress, view,
                scheduler, eventProcessor, eventChannel, masterAddressSupplierConsumer, onSuccess, onError);
    }

    public static void createViewer(
            final String userName,
            final Config config,
            final InetSocketAddress hostAddress,
            final SnakesGameView view,
            final Scheduler scheduler,
            final EventProcessor eventProcessor,
            final EventChannel eventChannel,
            final Consumer<Supplier<InetSocketAddress>> masterAddressSupplierConsumer,
            final Runnable onSuccess,
            final Consumer<String> onError) throws InterruptedException
    {
        createClient(
                SnakesProto.NodeRole.VIEWER, userName, config, hostAddress, view,
                scheduler, eventProcessor, eventChannel, masterAddressSupplierConsumer, onSuccess, onError);
    }

    private void bindControlsToRemote()
    {
        var keyBinds = this.view.getKeyBindingsRegisterer();

        keyBinds.accept(KeyEvent.VK_UP, () -> this.submitSteerMessage(Direction.UP));
        keyBinds.accept(KeyEvent.VK_W, () -> this.submitSteerMessage(Direction.UP));

        keyBinds.accept(KeyEvent.VK_DOWN, () -> this.submitSteerMessage(Direction.DOWN));
        keyBinds.accept(KeyEvent.VK_S, () -> this.submitSteerMessage(Direction.DOWN));

        keyBinds.accept(KeyEvent.VK_LEFT, () -> this.submitSteerMessage(Direction.LEFT));
        keyBinds.accept(KeyEvent.VK_A, () -> this.submitSteerMessage(Direction.LEFT));

        keyBinds.accept(KeyEvent.VK_RIGHT, () -> this.submitSteerMessage(Direction.RIGHT));
        keyBinds.accept(KeyEvent.VK_D, () -> this.submitSteerMessage(Direction.RIGHT));
    }

    private void bindControlsToLocal()
    {
        var keyBinds = this.view.getKeyBindingsRegisterer();
        var ownSnake = this.steerableByPlayerId.get(this.id);
        if (ownSnake == null)
        {
            throw new IllegalStateException("Own snake does not exist locally");
        }
        keyBinds.accept(KeyEvent.VK_UP, () -> ownSnake.changeDirection(Direction.UP));
        keyBinds.accept(KeyEvent.VK_W, () -> ownSnake.changeDirection(Direction.UP));

        keyBinds.accept(KeyEvent.VK_DOWN, () -> ownSnake.changeDirection(Direction.DOWN));
        keyBinds.accept(KeyEvent.VK_S, () -> ownSnake.changeDirection(Direction.DOWN));

        keyBinds.accept(KeyEvent.VK_LEFT, () -> ownSnake.changeDirection(Direction.LEFT));
        keyBinds.accept(KeyEvent.VK_A, () -> ownSnake.changeDirection(Direction.LEFT));

        keyBinds.accept(KeyEvent.VK_RIGHT, () -> ownSnake.changeDirection(Direction.RIGHT));
        keyBinds.accept(KeyEvent.VK_D, () -> ownSnake.changeDirection(Direction.RIGHT));
    }

    private void nextState() throws Exception
    {
        synchronized (this.lock)
        {
            if (this.role != SnakesProto.NodeRole.MASTER)
            {
                logger.warning("Not a master: must not calculate next state");
                return;
            }

            this.gameModel.nextState();
            var stateMessage = this.gameModel.getState().toMessage();
            this.gameModel.forEachDeadSnakeOwner(
                    it -> {
                        logger.info("Snake owned by player " + it.getId() + " has died");
                        if (this.id == it.getId())
                        {
                            logger.info("Snake associated with this node has died");
                            this.view.executeLeaveHooks();
                        } else
                        {
                            var deadSnakeOwner = new InetSocketAddress(it.getAddress(), it.getPort());
                            this.submitRoleChangeMessage(
                                    deadSnakeOwner,
                                    SnakesProto.NodeRole.VIEWER);
                            this.onPlayerLeft(deadSnakeOwner);
                        }
                    });
            this.view.updateView();
            this.submitState(stateMessage);
        }
    }

    private void handlePingMessage(
            final AddressedMessage message) throws InterruptedException
    {
        synchronized (this.lock)
        {
            var fromAddress = message.getAddress();
            var gameMessage = message.getMessage();

            if (!gameMessage.hasPing())
            {
                logger.warning("Non-ping message passed to ping message handler");
                return;
            }

            var senderId = this.playerIdByAddress.get(fromAddress);
            if (senderId != null)
            {
                this.submitAcknowledged(fromAddress, gameMessage.getMsgSeq(), senderId);
            }
        }
    }

    private void handleRoleChangeMessage(
            final AddressedMessage message) throws Exception
    {
        synchronized (this.lock)
        {
            var fromAddress = message.getAddress();
            var gameMessage = message.getMessage();

            if (!gameMessage.hasRoleChange())
            {
                logger.info("Non role change message passed to role change message handler");
                return;
            }

            logger.info("Got role change message from " + fromAddress);

            var roleChangeMessage = gameMessage.getRoleChange();
            if (roleChangeMessage.hasReceiverRole())
            {
                var receiverRole = roleChangeMessage.getReceiverRole();

                if (receiverRole == SnakesProto.NodeRole.DEPUTY)
                {
                    if (this.role == SnakesProto.NodeRole.MASTER)
                    {
                        logger.info("Role change message: master must not be demoted");
                        return;
                    }

                    logger.info("This node is now the deputy");
                    this.role = SnakesProto.NodeRole.DEPUTY;
                }

                if (receiverRole == SnakesProto.NodeRole.MASTER)
                {
                    if (this.role == SnakesProto.NodeRole.DEPUTY)
                    {
                        if (roleChangeMessage.hasSenderRole()
                                && roleChangeMessage.getSenderRole() == SnakesProto.NodeRole.VIEWER
                                && fromAddress.equals(this.masterAddress))
                        {
                            logger.info("Master left");
                            // this.eventChannel.submit(new NodeTimedOut(fromAddress));
                            this.onPlayerLeft(fromAddress);
                            // this.onPlayerDisconnected(fromAddress);
                        } else
                        {
                            logger.info(
                                    fromAddress + " says this node should become the new master but " +
                                            fromAddress + " is not a master and cannot issue such orders");
                        }
                    } else if (this.role == SnakesProto.NodeRole.NORMAL || this.role == SnakesProto.NodeRole.VIEWER)
                    {
                        logger.info(
                                fromAddress + " says this node should become the new master but this node is not" +
                                        " even a deputy");
                    }
                }

                if (receiverRole == SnakesProto.NodeRole.VIEWER)
                {
                    logger.info("Snake associated with this node has died");
                }

                this.role = receiverRole;
            }
            if (roleChangeMessage.hasSenderRole())
            {
                var senderRole = roleChangeMessage.getSenderRole();

                if (senderRole == SnakesProto.NodeRole.MASTER)
                {
                    logger.info(fromAddress + " (" + gameMessage.getSenderId() + ") is the new master");
                    this.masterAddress = fromAddress;
                    this.deputyAddress = null;
                    this.masterId = gameMessage.getSenderId();
                }

                if (senderRole == SnakesProto.NodeRole.VIEWER)
                {
                    // this.eventChannel.submit(new NodeTimedOut(fromAddress));
                    // this.onPlayerDisconnected(fromAddress);
                    this.onPlayerLeft(fromAddress);
                }
            }

            this.submitAcknowledged(fromAddress, gameMessage.getMsgSeq(), gameMessage.getSenderId());
        }
    }

    private void handleJoinMessage(
            final AddressedMessage message) throws InterruptedException
    {
        synchronized (this.lock)
        {
            if (this.role != SnakesProto.NodeRole.MASTER)
            {
                logger.info("Got join request but is not a master");
                return;
            }

            var fromAddress = message.getAddress();
            var gameMessage = message.getMessage();

            if (!gameMessage.hasJoin())
            {
                logger.warning("Non-join message passed to join message handler");
                return;
            }

            var joinMessage = gameMessage.getJoin();

            final int newPlayerId;
            @Nullable Steerable snake = null;
            newPlayerId = this.maxId + 1;

            var playerName = trimNameToFitMaxLength(joinMessage.getName());
            var playerDescriptor = new Player(
                    playerName, newPlayerId, fromAddress.getHostString(),
                    fromAddress.getPort(),
                    joinMessage.getOnlyView() ? SnakesProto.NodeRole.VIEWER : SnakesProto.NodeRole.NORMAL,
                    joinMessage.getPlayerType(),
                    0);

            if (!joinMessage.getOnlyView())
            {
                snake = this.gameModel.addPlayer(playerDescriptor, LocalSnake::new);
                if (snake == null)
                {
                    this.submitCannotJoinError(fromAddress);
                    logger.info("Failed to place a new snake for player " + playerName);
                    return; // <- if snake is null
                }
                this.view.setPreferredColour(newPlayerId, Colours.getRandomColour());
            }

            this.maxId += 1;
            this.submitAcknowledged(fromAddress, gameMessage.getMsgSeq(), newPlayerId);
            logger.info(
                    (joinMessage.getOnlyView() ? "Viewer " : "Player ")
                            + playerName + " " + newPlayerId + " joined");

            this.playerIdByAddress.put(fromAddress, newPlayerId);
            if (snake == null)
            {
                return;
            }
            this.steerableByPlayerId.put(newPlayerId, snake);
            if (this.deputyAddress == null)
            {
                this.chooseDeputy();
            }
        }
    }

    private void handleSteerMessage(
            final AddressedMessage message) throws InterruptedException
    {
        synchronized (this.lock)
        {
            if (this.role != SnakesProto.NodeRole.MASTER)
            {
                logger.info("Got steer request but is not a master");
                return;
            }

            var fromAddress = message.getAddress();
            var gameMessage = message.getMessage();

            if (!gameMessage.hasSteer())
            {
                logger.warning("Non-steer message passed to steer message handler");
                return;
            }

            var steerMessage = gameMessage.getSteer();

            var playerId = this.playerIdByAddress.get(fromAddress);
            if (playerId == null)
            {
                logger.info("Steer: No player with address " + fromAddress + " exists");
                //                    this.submitCannotJoinError(fromAddress, "Who are you?");
                return;
            }

            var snake = this.steerableByPlayerId.get(playerId);
            if (snake == null)
            {
                logger.info("Player " + playerId + " requests to steer their snake but they don't have one");
                // It's okay maybe they're just s l o w
                this.submitAcknowledged(fromAddress, gameMessage.getMsgSeq(), playerId);
                return;
            }

            var prevSeq = this.latestSteerSeq.get(playerId);

            if (prevSeq == null || gameMessage.getMsgSeq() > prevSeq)
            {
                var direction = Direction.valueOf(steerMessage.getDirection().toString());
                snake.changeDirection(direction);
                logger.fine("Steered snake for player " + playerId + " " + direction);
                this.latestSteerSeq.put(playerId, gameMessage.getMsgSeq());
            }

            this.submitAcknowledged(fromAddress, gameMessage.getMsgSeq(), playerId);
        }
    }

    private void handleStateMessage(
            final AddressedMessage message) throws InterruptedException
    {
        //        logger.info("Handling state");
        synchronized (this.lock)
        {
            if (this.role == SnakesProto.NodeRole.MASTER)
            {
                logger.info("Got state message but is the master");
                return;
            }

            var fromAddress = message.getAddress();
            var gameMessage = message.getMessage();

            if (!gameMessage.hasState())
            {
                logger.warning("Non-state message passed to state message handler");
                return;
            }

            if (!fromAddress.equals(this.getMasterAddress()))
            {
                logger.info("Received state message but not from master");
                return;
            }

            var stateMessage = gameMessage.getState();

            this.gameModel.setState(GameState.fromMessage(stateMessage.getState())); // TODO maybe check config i dunno
            this.gameModel.getNewIds().forEach(id -> {
                if (id != this.id)
                {
                    this.view.setPreferredColour(id, Colours.getRandomColour());
                }
            });
            this.gameModel.forEachPlayer(it -> {
                if (it.getRole() == SnakesProto.NodeRole.DEPUTY && this.deputyAddress == null)
                {
                    this.deputyAddress = new InetSocketAddress(it.getAddress(), it.getPort());
                }
            });
            this.view.updateView();
            logger.finest("Updated game state");
            this.submitAcknowledged(fromAddress, gameMessage.getMsgSeq(), gameMessage.getSenderId());
        }
    }

    private void submitSteerMessage(final Direction direction)
    {
        try
        {
            this.eventChannel.submit(
                    new OutgoingMessage(AddressedMessage.createMessageToMaster(
                            MessageFactory.createSteerMessage(direction))));
        } catch (final InterruptedException e)
        {
            logger.warning(Thread.currentThread().getName() + " interrupted when submitting steer message");
        }
    }

    private void submitState(final SnakesProto.GameState stateMessage) throws InterruptedException
    {
        synchronized (this.lock)
        {
            for (final var it : this.playerIdByAddress.keySet())
            {
                try
                {
                    //                    System.out.println("Sending state to " + it);
                    logger.finest("Sending state to " + it);
                    this.eventChannel.submit(
                            new OutgoingMessage(
                                    AddressedMessage.create(it, MessageFactory.createStateMessage(stateMessage))));
                } catch (final InterruptedException e)
                {
                    logger.info(Thread.currentThread().getName() + " Interrupted when submitting state message");
                    throw e;
                }
            }
        }
    }

    private void submitAnnouncement() throws InterruptedException
    {
        synchronized (this.lock)
        {
            var announcement = MessageFactory.createAnnouncementMessage(this.gameModel.getState());
            try
            {
                this.eventChannel.submit(
                        new OutgoingMessage(AddressedMessage.create(announceAddress, announcement)));
            } catch (final InterruptedException e)
            {
                logger.info(
                        Thread.currentThread().getName() + " Interrupted when submitting announcement message");
                throw e;
            }
        }
    }

    private void submitCannotJoinError(
            final InetSocketAddress address) throws InterruptedException
    {
        this.eventChannel.submit(
                new OutgoingMessage(
                        AddressedMessage.create(
                                address, MessageFactory.createErrorMessage("Cannot spawn another snake"))));
    }

    private void submitAcknowledged(
            final InetSocketAddress address,
            final long seq,
            final int toId) throws InterruptedException
    {
        this.eventChannel.submit(
                new OutgoingMessage(
                        AddressedMessage.create(
                                address, MessageFactory.createAcknowledgementMessage(seq, this.id, toId))));
    }

    private void submitRoleChangeMessage(
            final InetSocketAddress address,
            final SnakesProto.NodeRole receiverRole) throws InterruptedException
    {
        synchronized (this.lock)
        {
            var playerId = this.playerIdByAddress.get(address);
            if (playerId == null)
            {
                logger.info("Cannot send role changing message to " + address + ": player not connected");
                return;
            }
            this.eventChannel.submit(
                    new OutgoingMessage(
                            AddressedMessage.create(
                                    address,
                                    MessageFactory.createRoleChangingMessage(
                                            this.role, receiverRole, this.id, playerId))));
        }
    }

    private void submitRoleChangeMessage(
            final InetSocketAddress address) throws InterruptedException
    {
        synchronized (this.lock)
        {
            var playerId = address.equals(this.masterAddress)
                    ? Integer.valueOf(this.masterId)
                    : this.playerIdByAddress.get(address);
            if (playerId == null)
            {
                logger.info("Cannot send role changing message to " + address + ": no such player");
                return;
            }
            this.eventChannel.submit(
                    new OutgoingMessage(
                            AddressedMessage.create(
                                    address,
                                    MessageFactory.createRoleChangingMessage(
                                            this.role, this.id, playerId))));
        }
    }

    private void onPlayerLeft(final InetSocketAddress address)
            throws Exception
    {
        synchronized (this.lock)
        {
            logger.info(address + " left and became a viewer");

            if (this.role == SnakesProto.NodeRole.MASTER)
            {
                var playerId = this.playerIdByAddress.get(address);
                if (playerId == null)
                {
                    logger.info("Player left: no player with address " + address);
                    return;
                }

                this.steerableByPlayerId.remove(playerId);

                this.gameModel.playerLeft(playerId);

                if (address.equals(this.deputyAddress))
                {
                    logger.info("Deputy left");
                    this.deputyAddress = null;
                    this.chooseDeputy();
                }
            } else if (address.equals(this.masterAddress))
            {
                if (this.role == SnakesProto.NodeRole.DEPUTY)
                {
                    this.becomeMaster();
                    this.gameModel.playerLeft(this.masterId);
                } else if (this.deputyAddress != null)
                {
                    this.masterAddress = this.deputyAddress;
                    this.deputyAddress = null;
                    logger.info("Master left, " + this.masterAddress + " is the new master");
                }
                //            else {
                //                if (this.deputyAddress != null) {
                //                    logger.info(this.deputyAddress + " becomes the new master");
                //                    this.masterAddress = this.deputyAddress;
                //                    this.deputyAddress = null;
                //                } else {
                //                    logger.info(
                //                            "Master disconnected but there was no deputy (which is weird cause THIS node " +
                //                            "should have been the deputy)");
                //                }
                //            }
            }
        }
    }

    private void onPlayerDisconnected(final InetSocketAddress address)
            throws Exception
    {
        synchronized (this.lock)
        {
            logger.info(address + " disconnected");

            if (this.role == SnakesProto.NodeRole.MASTER)
            {
                var playerId = this.playerIdByAddress.get(address);
                if (playerId == null)
                {
                    logger.info("Player disconnected: no player with address " + address);
                    return;
                }

                this.playerIdByAddress.remove(address);

                this.steerableByPlayerId.remove(playerId);

                this.latestSteerSeq.remove(playerId);

                this.gameModel.playerLeft(playerId);

                if (address.equals(this.deputyAddress))
                {
                    logger.info("Deputy disconnected");
                    this.deputyAddress = null;
                    this.chooseDeputy();
                }
            } else if (address.equals(this.masterAddress))
            {
                if (this.role == SnakesProto.NodeRole.DEPUTY)
                {
                    this.becomeMaster();
                    this.gameModel.playerDisconnected(this.masterId);
                } else if (this.deputyAddress != null)
                {
                    this.masterAddress = this.deputyAddress;
                    this.deputyAddress = null;
                    logger.info("Master disconnected, " + this.masterAddress + " is the new master");
                }
                //            else {
                //                if (this.deputyAddress != null) {
                //                    logger.info(this.deputyAddress + " becomes the new master");
                //                    this.masterAddress = this.deputyAddress;
                //                    this.deputyAddress = null;
                //                } else {
                //                    logger.info(
                //                            "Master disconnected but there was no deputy (which is weird cause THIS node " +
                //                            "should have been the deputy)");
                //                }
                //            }
            }
        }
    }

    private void chooseDeputy() throws InterruptedException
    {
        synchronized (this.lock)
        {
            logger.info("Choosing deputy");
            var deputy = this.playerIdByAddress.entrySet().stream()
                    .filter(it -> {
                        if (it.getKey().equals(this.masterAddress))
                        {
                            return false; // ex master cannot become the deputy
                        }
                        if (this.gameModel.hasPlayerWithId(it.getValue()))
                        {
                            var player = this.gameModel.getPlayerById(it.getValue());
                            return player.getRole() == SnakesProto.NodeRole.NORMAL;
                        }
                        return false;
                    })
                    .map(Map.Entry::getKey)
                    .findAny()
                    .orElse(null);
            if (deputy == null)
            {
                logger.info("Cannot choose deputy: no connected players");
                return;
            }

            logger.info("Chosen deputy: " + deputy);
            this.deputyAddress = deputy;
            this.submitRoleChangeMessage(deputy, SnakesProto.NodeRole.DEPUTY);
        }
    }

    private void becomeMaster() throws Exception
    {
        synchronized (this.lock)
        {
            if (this.role == SnakesProto.NodeRole.MASTER)
            {
                logger.info("Already is the master");
                return;
            }

            logger.info("Becoming master");
            //            if (this.stateHandler != null) {
            //                this.stateHandler.remove();
            //            }

            this.role = SnakesProto.NodeRole.MASTER;

            logger.info("Collecting players information");
            logger.info("Own id " + this.id + ", master id " + this.masterId);
            var maxId = new int[]{this.id};
            this.gameModel.forEachPlayer(
                    it -> {
                        if (it.getId() > maxId[0])
                        {
                            maxId[0] = it.getId();
                        }
                        logger.info(it.toString());
                        if (it.getId() != this.id)
                        {
                            logger.info("Treating " + it.getId() + " as connected");
                            if (this.masterId == it.getId() && this.masterAddress != null)
                            {
                                this.playerIdByAddress.put(this.masterAddress, it.getId());
                                var playerDescriptor = this.gameModel.getPlayerById(it.getId());
                                playerDescriptor.setAddress(this.masterAddress.getAddress().getHostAddress());
                                playerDescriptor.setPort(this.masterAddress.getPort());
                                logger.info("Real ex-master address is set for player " + it.getId());
                            } else if (!it.getAddress().isEmpty())
                            {
                                this.playerIdByAddress.put(
                                        new InetSocketAddress(it.getAddress(), it.getPort()), it.getId());
                            } else
                            {
                                logger.warning("Empty address but id is not the master id");
                            }
                        }
                    });
            this.gameModel.forEachSnake(
                    it -> {
                        if (!it.isZombie())
                        {
                            this.steerableByPlayerId.put(it.getPlayerId(),
                                    this.gameModel.getSnakeById(it.getPlayerId()));

                        } /* else {
                            logger.info(
                                    "Removing player " + it.getPlayerId() + " because they already were offline");
                            this.playerIdByAddress.keySet().removeIf(
                                    address -> this.playerIdByAddress.get(address) == it.getPlayerId());
                         }*/
                    });
            if (this.gameModel.hasSnakeWithPlayerId(this.id))
            {
                this.bindControlsToLocal();
            }

            this.maxId = maxId[0];

            var joinHandler = this.eventProcessor.addHandler(
                    event -> event instanceof IncomingMessage
                            && event.<IncomingMessage>get().message.getMessage().hasJoin(),
                    event -> this.handleJoinMessage(event.<IncomingMessage>get().message));
            this.view.getExitHookRegisterer().accept(joinHandler::remove);
            this.view.getLeaveHookRegisterer().accept(joinHandler::remove);

            var steerHandler = this.eventProcessor.addHandler(
                    event -> event instanceof IncomingMessage
                            && event.<IncomingMessage>get().message.getMessage().hasSteer(),
                    event -> this.handleSteerMessage(event.<IncomingMessage>get().message));
            this.view.getExitHookRegisterer().accept(steerHandler::remove);

            var stateTask = this.scheduler.schedule(this::nextState, this.stateDelayMs);
            var announceTask = this.scheduler.schedule(this::submitAnnouncement, ANNOUNCE_DELAY_MS);

            this.view.getLeaveHookRegisterer().accept(() -> {
                synchronized (this.gameModel)
                {
                    var pastRole = this.role;
                    this.role = SnakesProto.NodeRole.VIEWER;
                    stateTask.cancel();
                    announceTask.cancel();
                    this.gameModel.playerLeft(this.id);
                    this.view.updateView();

                    if (pastRole == SnakesProto.NodeRole.MASTER && this.deputyAddress != null)
                    {
                        this.masterAddress = this.deputyAddress;
                        this.deputyAddress = null;
                        try
                        {
                            this.submitRoleChangeMessage(this.masterAddress, SnakesProto.NodeRole.MASTER);
                            this.bindControlsToRemote();
                        } catch (final InterruptedException e)
                        {
                            logger.info(
                                    Thread.currentThread().getName() + " interrupted when submitting role change " +
                                            "message after leaving");
                        }
                    }
                }
            });

            this.view.getExitHookRegisterer().accept(stateTask::cancel);
            this.view.getExitHookRegisterer().accept(announceTask::cancel);

            logger.info("Telling others about own promotion");
            for (final var it : this.playerIdByAddress.keySet())
            {
                logger.info("Telling " + it + " about own promotion");
                this.submitRoleChangeMessage(it);
            }

            this.deputyAddress = null;
            this.chooseDeputy();
            this.masterAddress = null;
        }
    }

    private @Nullable InetSocketAddress getMasterAddress()
    {
        synchronized (this.lock)
        {
            return this.masterAddress;
        }
    }

    private static String trimNameToFitMaxLength(final String name)
    {
        if (name.length() <= Constants.MAX_NAME_LENGTH)
        {
            return name;
        }
        return name.substring(0, Constants.MAX_NAME_LENGTH - 3) + "...";
    }
}
