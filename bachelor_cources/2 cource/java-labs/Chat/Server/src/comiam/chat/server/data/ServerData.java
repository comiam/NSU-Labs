package comiam.chat.server.data;

import com.google.gson.reflect.TypeToken;
import comiam.chat.server.core.ServerCore;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.time.Date;
import comiam.chat.server.utils.Hash;
import comiam.chat.server.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static comiam.chat.server.json.JSONCore.parseFromFile;
import static comiam.chat.server.json.JSONCore.saveToFile;

public class ServerData
{
    private static final ArrayList<User> users = new ArrayList<>();
    private static final ArrayList<Chat> chats = new ArrayList<>();
    private static final User serverNotifier = new User(Hash.hashBytes("12132455432434".getBytes()), "server");

    public static User getServerNotifier()
    {
        return serverNotifier;
    }

    public static boolean loadData(String databasePath)
    {
        Log.info("Loading database...");
        System.out.println("Loading database...");

        if(databasePath.equals("null"))
        {
            Log.info("In configuration file typed empty database!");
            System.out.println("In configuration file typed empty database!");

            Log.info("Created empty database.");
            System.out.println("Created empty database.");
            return true;
        }

        File file = new File(databasePath);
        if(!file.exists())
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

        Pair<ArrayList<User>, ArrayList<Chat>> data = parseFromFile(databasePath, new TypeToken<Pair<ArrayList<User>, ArrayList<Chat>>>(){}.getType());
        if(data == null)
        {
            System.out.println("Error when upload database. Shutting down...");
            Log.error("Error when upload database: Shutting down...");
            return false;
        }

        if(data.getFirst() != null)
            users.addAll(data.getFirst());
        if(data.getSecond() != null)
            chats.addAll(data.getSecond());

        System.out.println("Database loaded successfully!");
        Log.info("Database loaded successfully!");

        return true;
    }

    public static void saveData(String databasePath)
    {
        Log.info("Saving database...");

        if(databasePath == null || databasePath.equals("null"))
            ServerCore.setNewDBPath(databasePath = "newDB" + Date.getDate());

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

        Pair<ArrayList<User>, ArrayList<Chat>> pair = new Pair<>(users, chats);

        saveToFile(file, pair);
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

        return res.isEmpty() ? null : (Chat[]) res.toArray(new Chat[res.size()]);
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
