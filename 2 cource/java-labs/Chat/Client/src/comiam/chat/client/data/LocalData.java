package comiam.chat.client.data;

public class LocalData
{
    private static String currentSessionID;
    private static String username;
    private static String password;
    private static boolean disconnected = false;

    public static boolean isDisconnected()
    {
        return disconnected;
    }

    public static void setDisconnected(boolean disconnected)
    {
        LocalData.disconnected = disconnected;
    }

    public static void setPassword(String password)
    {
        LocalData.password = password;
    }

    public static void setUsername(String username)
    {
        LocalData.username = username;
    }

    public static String getPassword()
    {
        return password;
    }

    public static String getUsername()
    {
        return username;
    }

    public static String getCurrentSessionID()
    {
        return currentSessionID;
    }

    public static void setCurrentSessionID(String sessionID)
    {
        currentSessionID = sessionID;
    }
}
