package comiam.snakegame.network.message;

import me.ippolitov.fit.snakes.SnakesProto;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public final class AddressedMessage
{
    private static final int MAX_RETRIES_COUNT = 3;

    private @Nullable InetSocketAddress address;
    private final SnakesProto.GameMessage message;
    private int retriesLeft = MAX_RETRIES_COUNT;

    private AddressedMessage(
            final SnakesProto.GameMessage message)
    {
        this.address = null;
        this.message = message;
    }

    private AddressedMessage(
            final InetSocketAddress address,
            final SnakesProto.GameMessage message)
    {
        this.address = address;
        this.message = message;
    }

    public static AddressedMessage createMessageToMaster(
            final SnakesProto.GameMessage message)
    {
        return new AddressedMessage(message);
    }

    public static AddressedMessage create(
            final InetSocketAddress address,
            final SnakesProto.GameMessage message)
    {
        return new AddressedMessage(address, message);
    }

    public boolean isAddressedToMaster()
    {
        return this.address == null;
    }
    
    public InetSocketAddress getAddress()
    {
        if (this.address == null)
        {
            throw new IllegalStateException("No destination address");
        }
        return this.address;
    }
    
    public SnakesProto.GameMessage getMessage()
    {
        return this.message;
    }

    public void decrementRetriesCount()
    {
        this.retriesLeft -= 1;
    }

    public boolean retriesLeft()
    {
        return this.retriesLeft > 0;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other == null)
        {
            return false;
        }
        if (other.getClass() != this.getClass())
        {
            return false;
        }
        return this.message.getMsgSeq() == ((AddressedMessage) other).message.getMsgSeq();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.message.getMsgSeq());
    }
}
