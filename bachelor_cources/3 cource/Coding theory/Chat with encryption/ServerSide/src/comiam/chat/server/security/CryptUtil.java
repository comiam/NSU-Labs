package comiam.chat.server.security;

import comiam.chat.server.data.units.User;

public class CryptUtil
{
    public static String encrypt(User user, String msg)
    {
        try
        {
            return AESCipher.encryptMsg(msg, KeyDB.getKeyOf(user));
        }catch (Throwable e)
        {
            return "error encrypt";
        }
    }
}
