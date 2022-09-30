package comiam.snakegame.gamelogic.gameobjects.config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public interface Config extends GameConfig, NetworkConfig
{
    Path CONFIG_FILE_PATH = Paths.get(
            System.getProperty("user.dir"), "config", "config.json");
    String CONFIG_FILE_NAME = CONFIG_FILE_PATH.toString();

    Charset CHARSET = StandardCharsets.UTF_8;

    int DEFAULT_FOOD_STATIC = 1;
    int DEFAULT_HEIGHT = 30;
    int DEFAULT_WIDTH = 40;
    int DEFAULT_NODE_TIMEOUT_MS = 3_000;
    int DEFAULT_PING_DELAY_MS = 1_000;
    int DEFAULT_STEP_DELAY_MS = 1_000;
    float DEFAULT_PER_PLAYER = DEFAULT_FOOD_STATIC;
    float DEFAULT_FOOD_SPAWN_ON_DEATH_CHANCE = 0.1f;

    Config DEFAULT_CONFIG = new ConfigValue();

    static Config load() throws InvalidConfigException
    {
        try (
                var in = new FileReader(CONFIG_FILE_NAME, CHARSET);
                var jsonReader = new JsonReader(in))
        {
            var g = new Gson();
            try
            {
                final Config config = g.fromJson(jsonReader, ConfigValue.class);
                ConfigValidator.isValid(config);
                return config;
            } catch (final JsonSyntaxException e)
            {
                throw new InvalidConfigException(e.getMessage());
            } catch (final JsonIOException e)
            {
                throw new IOException(e);
            }
        } catch (final FileNotFoundException e)
        {
            Logger.getLogger(Config.class.getSimpleName()).info("Config not found in " + CONFIG_FILE_PATH);
            DEFAULT_CONFIG.store();
            return DEFAULT_CONFIG;
        } catch (final IOException e)
        {
            Logger.getLogger(Config.class.getSimpleName()).warning("Unexpected IOException occurred when loading config");
            DEFAULT_CONFIG.store();
            return DEFAULT_CONFIG;
        }
    }

    default void store()
    {
        var dir = CONFIG_FILE_PATH.getParent();
        var dirFile = dir.toFile();
        if (!dirFile.exists())
        {
            var success = dir.toFile().mkdirs();
            if (!success)
            {
                Logger.getLogger(Config.class.getSimpleName()).warning("Failed to create parent directories for config file");
                return;
            }
        } else if (!dirFile.isDirectory())
        {
            Logger.getLogger(Config.class.getSimpleName()).warning("Cannot create config file in " + dir.toString() + ": not a directory");
            return;
        }

        try (var out = new FileWriter(CONFIG_FILE_NAME, CHARSET))
        {
            var g = new Gson();
            var jsonConfig = g.toJson(this);
            out.write(jsonConfig);
            return;
        } catch (final FileNotFoundException e)
        {
            Logger.getLogger(Config.class.getSimpleName()).info("Cannot open file " + CONFIG_FILE_PATH);
        } catch (final IOException e)
        {
            Logger.getLogger(Config.class.getSimpleName()).warning("Unexpected IOException occurred when storing config");
        }

        Logger.getLogger(Config.class.getSimpleName()).info("Created default config " + CONFIG_FILE_NAME);
    }

    static Config fromMessage(final SnakesProto.GameConfigOrBuilder config)
    {
        var result = new ConfigValue();
        result.setPlaneWidth(config.getWidth());
        result.setPlaneHeight(config.getHeight());
        result.setFoodStatic(config.getFoodStatic());
        result.setFoodPerPlayer(config.getFoodPerPlayer());
        result.setFoodSpawnOnDeathChance(config.getDeadFoodProb());
        result.setStateDelayMs(config.getStateDelayMs());
        result.setPingDelayMs(config.getPingDelayMs());
        result.setNodeTimeoutMs(config.getNodeTimeoutMs());
        return result;
    }

    default SnakesProto.GameConfig toMessage()
    {
        return SnakesProto.GameConfig.newBuilder()
                .setWidth(this.getPlaneWidth())
                .setHeight(this.getPlaneHeight())
                .setFoodStatic(this.getFoodStatic())
                .setFoodPerPlayer(this.getFoodPerPlayer())
                .setDeadFoodProb(this.getFoodSpawnOnDeathChance())
                .setStateDelayMs(this.getStateDelayMs())
                .setPingDelayMs(this.getPingDelayMs())
                .setNodeTimeoutMs(this.getNodeTimeoutMs())
                .build();
    }
}

