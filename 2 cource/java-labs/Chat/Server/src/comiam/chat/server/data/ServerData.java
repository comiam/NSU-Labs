package comiam.chat.server.data;

import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.xml.XMLCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ServerData
{
    private static final ArrayList<User> users = new ArrayList<>();
    private static final ArrayList<Chat> chats = new ArrayList<>();

    public static boolean loadData(String databasePath)
    {
        Log.info("Loading database...");
        System.out.println("Loading database...");

        File file = new File(databasePath);
        if(file.exists())
        {
            System.out.println("Database file not exist. Shutting down...");
            Log.error("Database file not exist. Shutting down...");
            return false;
        }

        if(!file.canRead())
        {
            System.out.println("Can't read database file. Shutting down...");
            Log.error("Can't read database file. Shutting down...");
            return false;
        }

        var data = XMLCore.loadDatabase(file);

        if(data == null)
        {
            System.out.println("Error when upload database: " + XMLCore.getParserError() + ". Shutting down...");
            Log.error("Error when upload database: " + XMLCore.getParserError() + ". Shutting down...");
            return false;
        }

        users.addAll(data.getFirst());
        chats.addAll(data.getSecond());

        System.out.println("Database loaded successfully!");
        Log.info("Database loaded successfully!");

        return true;
    }

    public static void saveData(String databasePath)
    {
        Log.info("Saving database...");
        File file = new File(databasePath);

        if(!file.exists())
            try
            {
                file.createNewFile();
            } catch(IOException e)
            {
                System.out.println("Can't save database in the specified folder! Data lost...");
                Log.error("Can't save database in the specified folder! Data lost...", e);
                return;
            }

        if(!file.canWrite())
        {
            System.out.println("Can't write to specified database file. Data lost...");
            Log.error("Can't write to database file. Data lost...");
            return;
        }

        XMLCore.saveDatabase(file, users, chats);
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
