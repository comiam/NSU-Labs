package comiam.snakegame.gamelogic.gameobjects.player;

import me.ippolitov.fit.snakes.SnakesProto;

public interface PlayerInfo
{

    String getName();

    int getId();

    String getAddress();

    int getPort();

    SnakesProto.NodeRole getRole();

    SnakesProto.PlayerType getType();

    int getScore();

    static PlayerInfo fromMessage(final SnakesProto.GamePlayerOrBuilder player)
    {
        return new Player(
                player.getName(),
                player.getId(),
                player.getIpAddress(),
                player.getPort(),
                player.getRole(),
                player.getType(),
                player.getScore());
    }

    default SnakesProto.GamePlayer toMessage()
    {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(this.getName())
                .setId(this.getId())
                .setIpAddress(this.getAddress())
                .setPort(this.getPort())
                .setRole(this.getRole())
                .setType(this.getType())
                .setScore(this.getScore())
                .build();
    }
}
