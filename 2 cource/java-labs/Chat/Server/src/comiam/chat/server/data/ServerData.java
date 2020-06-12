package comiam.chat.server.data;

import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;

import java.util.ArrayList;

public class ServerData
{
    private static final ArrayList<User> users = new ArrayList<>();
    private static final ArrayList<Chat> chats = new ArrayList<>();

    public static void loadData()
    {

    }

    public static void saveData()
    {

    }

    public static Chat getChatByName(String name)
    {
        synchronized(chats)
        {
            for(var ch : chats)
                if(ch.getName().equals(name))
                    return ch;
            return null;
        }
    }

    public static boolean isUserHaveChat(User user)
    {
        return getUserChatList(user) != null;
    }

    public static Chat[] getUserChatList(User user)
    {
        ArrayList<Chat> res = new ArrayList<>();

        for(var chat : chats)
            if(chat.containsUser(user))
                res.add(chat);

        return res.isEmpty() ? null : (Chat[]) res.toArray();
    }

    public static boolean containsChat(String name)
    {
        synchronized(chats)
        {
            for(var ch : chats)
                if(ch.getName().equals(name))
                    return true;
            return false;
        }
    }

    public static void addNewChat(Chat chat)
    {
        synchronized(chats)
        {
            chats.add(chat);
        }
    }

    public static ArrayList<Chat> getChats()
    {
        return chats;
    }

    public static User getUserByName(String name)
    {
        synchronized(users)
        {
            for(var user : users)
                if(user.getUsername().equals(name))
                    return user;

            return null;
        }
    }

    public static ArrayList<User> getUsers()
    {
        return users;
    }

    public static void addNewUser(User user)
    {
        synchronized(users)
        {
            users.add(user);
        }
    }

    public static boolean containsUsername(String name)
    {
        return getUserByName(name) != null;
    }
}
