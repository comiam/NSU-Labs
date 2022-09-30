package comiam.snakegame.network.message;

import comiam.snakegame.gamelogic.gameobjects.game.GameState;
import comiam.snakegame.gamelogic.gamefield.Direction;
import me.ippolitov.fit.snakes.SnakesProto;

import java.util.concurrent.atomic.AtomicLong;

public final class MessageFactory
{
    private static final AtomicLong sequenceNumber = new AtomicLong(0);

    private MessageFactory()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static SnakesProto.GameMessage.Builder createBuilderWithSequenceNumber()
    {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(sequenceNumber.getAndIncrement());
    }

    public static SnakesProto.GameMessage createPingMessage()
    {
        return createBuilderWithSequenceNumber()
                .setPing(
                        SnakesProto.GameMessage.PingMsg.newBuilder()
                                .build())
                .build();
    }

    public static SnakesProto.GameMessage createSteerMessage(final Direction direction)
    {
        return createBuilderWithSequenceNumber()
                .setSteer(
                        SnakesProto.GameMessage.SteerMsg.newBuilder()
                                .setDirection(SnakesProto.Direction.valueOf(direction.toString()))
                                .build())
                .build();
    }

    public static SnakesProto.GameMessage createAcknowledgementMessage(
            final long sequenceNumber,
            final int senderId,
            final int receiverId)
    {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(sequenceNumber)
                .setAck(
                        SnakesProto.GameMessage.AckMsg.newBuilder()
                                .build())
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage createStateMessage(final SnakesProto.GameState state)
    {
        return createBuilderWithSequenceNumber()
                .setState(
                        SnakesProto.GameMessage.StateMsg.newBuilder()
                                .setState(state)
                                .build())
                .build();
    }

    public static SnakesProto.GameMessage createAnnouncementMessage(
            final GameState state)
    {
        var playersBuilder = SnakesProto.GamePlayers.newBuilder();
        state.getPlayers().forEach(it -> playersBuilder.addPlayers(it.toMessage()));

        return createBuilderWithSequenceNumber()
                .setAnnouncement(
                        SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                                .setConfig(state.getConfig().toMessage())
                                .setPlayers(playersBuilder.build()))
                .build();

    }

    public static SnakesProto.GameMessage createJoinMessage(
            final String name,
            final boolean isBot,
            final boolean watchOnly)
    {
        return createBuilderWithSequenceNumber()
                .setJoin(
                        SnakesProto.GameMessage.JoinMsg.newBuilder()
                                .setName(name)
                                .setOnlyView(watchOnly)
                                .setPlayerType(isBot ? SnakesProto.PlayerType.ROBOT : SnakesProto.PlayerType.HUMAN)
                                .build())
                .build();
    }

    public static SnakesProto.GameMessage createErrorMessage(final String what)
    {
        return createBuilderWithSequenceNumber()
                .setError(
                        SnakesProto.GameMessage.ErrorMsg.newBuilder()
                                .setErrorMessage(what)
                                .build())
                .build();
    }

    public static SnakesProto.GameMessage createRoleChangingMessage(
            final SnakesProto.NodeRole senderRole,
            final SnakesProto.NodeRole receiverRole,
            final int senderId,
            final int receiverId)
    {
        return createBuilderWithSequenceNumber()
                .setRoleChange(
                        SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                                .setSenderRole(senderRole)
                                .setReceiverRole(receiverRole)
                                .build())
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage createRoleChangingMessage(
            final SnakesProto.NodeRole senderRole,
            final int senderId,
            final int receiverId)
    {
        return createBuilderWithSequenceNumber()
                .setRoleChange(
                        SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                                .setSenderRole(senderRole)
                                .build())
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
    }
}
