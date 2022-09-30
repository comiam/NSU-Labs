package comiam.snakegame.gamelogic.gameobjects.player;

import me.ippolitov.fit.snakes.SnakesProto;

public class Player implements PlayerInfo
{
    private final String name;
    private final int id;
    private String address;
    private int port;
    private SnakesProto.NodeRole role;
    private final SnakesProto.PlayerType type;
    private int score;

    public Player(String name, int id, String address, int port, SnakesProto.NodeRole role, SnakesProto.PlayerType type, int score)
    {
        this.name = name;
        this.id = id;
        this.address = address;
        this.port = port;
        this.role = role;
        this.type = type;
        this.score = score;
    }

    public static Player copyOf(final PlayerInfo other)
    {
        return new Player(other);
    }

    private Player(final PlayerInfo other)
    {
        this(
                other.getName(), other.getId(), other.getAddress(),
                other.getPort(), other.getRole(), other.getType(), other.getScore());
    }

    public void setRole(final SnakesProto.NodeRole newRole)
    {
        this.role = newRole;
    }

    public void increaseScore()
    {
        this.score += 1;
    }

    public String getName()
    {
        return this.name;
    }

    public int getId()
    {
        return this.id;
    }

    public String getAddress()
    {
        return this.address;
    }

    public int getPort()
    {
        return this.port;
    }

    public SnakesProto.NodeRole getRole()
    {
        return this.role;
    }

    public SnakesProto.PlayerType getType()
    {
        return this.type;
    }

    public int getScore()
    {
        return this.score;
    }

    public boolean equals(final Object o)
    {
        if (o == this) return true;
        if (!(o instanceof Player)) return false;
        final Player other = (Player) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getId() != other.getId()) return false;
        return true;
    }

    protected boolean canEqual(final Object other)
    {
        return other instanceof Player;
    }

    public int hashCode()
    {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        return result;
    }

    public String toString()
    {
        return "PlayerDescriptor(name=" + this.getName() + ", id=" + this.getId() + ", address=" + this.getAddress() + ", port=" + this.getPort() + ", role=" + this.getRole() + ", type=" + this.getType() + ", score=" + this.getScore() + ")";
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
