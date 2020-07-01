package comiam.chat.server.data.units;

import comiam.chat.server.data.session.Sessions;

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

    public boolean isOnline()
    {
        return Sessions.getSession(this) != null;
    }
}
