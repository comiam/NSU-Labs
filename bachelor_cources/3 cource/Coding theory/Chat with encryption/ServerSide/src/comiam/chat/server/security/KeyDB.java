package comiam.chat.server.security;

import comiam.chat.server.data.units.User;

import java.math.BigInteger;
import java.util.HashMap;

public class KeyDB
{
    private static final HashMap<User, BigInteger> keys = new HashMap<>();

    public static boolean containsKey(User user)
    {
        return keys.containsKey(user);
    }

    public static void removeKey(User user)
    {
        keys.remove(user);
    }

    public static void emplaceKey(User user, BigInteger secret)
    {
        keys.put(user, secret);
    }

    public static BigInteger getKeyOf(User user)
    {
        return keys.get(user);
    }
}

