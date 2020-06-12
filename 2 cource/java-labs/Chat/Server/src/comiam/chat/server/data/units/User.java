package comiam.chat.server.data.units;

public class User
{
    private final String passHash;
    private final String userName;
    private final String lastActive;

    public User(String passHash, String userName, String lastActive)
    {
        this.passHash = passHash;
        this.userName = userName;
        this.lastActive = lastActive;
    }

    public String getPassHash()
    {
        return passHash;
    }

    public String getUsername()
    {
        return userName;
    }

    public String getLastActive()
    {
        return lastActive;
    }
}
