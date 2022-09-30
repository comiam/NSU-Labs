package comiam.chat.client.security;

import java.math.BigInteger;
import java.util.HashMap;

public class KeyDB
{
    public static BigInteger key;

    public static BigInteger getKey()
    {
        return key;
    }

    public static void setKey(BigInteger key)
    {
        KeyDB.key = key;
    }
}

