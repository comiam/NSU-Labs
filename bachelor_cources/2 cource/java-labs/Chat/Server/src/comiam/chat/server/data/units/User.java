package comiam.chat.server.data.units;

public class User
{
    private final String passHash;
    private final String userName;

    public User(String passHash, String userName)
    {
        this.passHash = passHash;
        this.userName = userName;
    }

    public boolean equals(User another)
    {
        if(another == null)
            return false;
        return this.passHash.equals(another.passHash) && this.userName.equals(another.userName);
    }

    public String getPassHash()
    {
        return passHash;
    }

    public String getUsername()
    {
        return userName;
    }
}
