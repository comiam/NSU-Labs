package comiam.chat.server.data.units;

import comiam.chat.server.time.Date;

import java.util.ArrayList;

public class Chat
{
    private final String             name;
    private final String             dateCreate;
    private final ArrayList<User>    users = new ArrayList<>();
    private final ArrayList<Message> messages = new ArrayList<>();

    public Chat(String name, ArrayList<User> users, ArrayList<Message> messages)
    {
        this.name  = name;
        this.dateCreate = Date.getDate();

        if(users != null)
            this.users.addAll(users);
        if(messages != null)
            this.messages.addAll(messages);
    }

    public String getDateOfCreation()
    {
        return dateCreate;
    }

    public String getName()
    {
        return name;
    }

    public int getUserSize()
    {
        return users.size();
    }

    public void addMessage(Message message)
    {
        messages.add(message);
    }

    public boolean containsUser(User user)
    {
        return users.contains(user);
    }

    public void addUser(User user)
    {
        users.add(user);
    }

    public ArrayList<User> getUsers()
    {
        return users;
    }

    public ArrayList<Message> getMessages()
    {
        return messages;
    }
}

