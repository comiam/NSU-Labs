package comiam.chat.client.data.units;

public class User
{
    private final String userName;
    private boolean online = false;

    public User(String userName)
    {
        this.userName = userName;
    }

    public String getUsername()
    {
        return userName;
    }

    public void setOnline(boolean online)
    {
        this.online = online;
    }

    public boolean isOnline()
    {
        return online;
    }
}
