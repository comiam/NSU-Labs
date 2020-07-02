package comiam.chat.server.main;

public class ConsoleCommands
{
    public static final String SHUTDOWN_SERVER = "shutdown";

    public static boolean isValidCommand(String command)
    {
        return command.equals(SHUTDOWN_SERVER);
    }
}
