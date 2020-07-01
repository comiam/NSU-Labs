package comiam.chat.client.data;

import comiam.chat.client.data.units.Chat;

import java.util.ArrayList;

public class LocalData
{
    private static String currentSessionID;
    private static ArrayList<Chat> chats = new ArrayList<>();

    public static String getCurrentSessionID()
    {
        return currentSessionID;
    }

    public static void setCurrentSessionID(String sessionID)
    {
        currentSessionID = sessionID;
    }
}
