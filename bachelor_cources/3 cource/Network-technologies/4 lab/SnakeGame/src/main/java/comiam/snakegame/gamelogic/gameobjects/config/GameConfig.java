package comiam.snakegame.gamelogic.gameobjects.config;

public interface GameConfig
{
    int getPlaneWidth();
    int getPlaneHeight();
    int getFoodStatic();
    float getFoodPerPlayer();
    int getStateDelayMs();
    float getFoodSpawnOnDeathChance();
}
