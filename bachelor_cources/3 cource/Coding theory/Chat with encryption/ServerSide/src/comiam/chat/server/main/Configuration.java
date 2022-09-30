package comiam.chat.server.main;

public class Configuration
{
    private final int port;
    private final String dbPath;
    private final boolean logging;

    public Configuration(int port, String dbPath, boolean logging)
    {
        this.port = port;
        this.dbPath = dbPath;
        this.logging = logging;
    }

    public int getPort()
    {
        return port;
    }

    public boolean isLogging()
    {
        return logging;
    }

    public String getDbPath()
    {
        return dbPath;
    }
}
