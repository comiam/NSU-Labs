package comiam.chat.client.data;

import comiam.chat.client.utils.Pair;

import java.util.ArrayList;

public class LocalData
{
    private static String currentSessionID;
    private static ArrayList<Pair<String, Integer>> cachedChats = new ArrayList<>();
    private static String username;

    public static void setUsername(String username)
    {
        LocalData.username = username;
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

    public static ArrayList<Pair<String, Integer>> getCachedChats()
    {
        return cachedChats;
    }

    public static void setCachedChats(ArrayList<Pair<String, Integer>> cachedChats)
    {
        LocalData.cachedChats = cachedChats;
    }
}
