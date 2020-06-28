package comiam.chat.server.main;

public class ConsoleCommands
{
    public static final String SHUTDOWN_SERVER = "shutdown";
    /* Здесь могли быть ваши команды от консоли сервера, который нахрен кому сдался.
       Бля, поставьте 5 автоматом((9(99((9(( */

    public static boolean isValidCommand(String command)
    {
        return command.equals(SHUTDOWN_SERVER);
    }
}
