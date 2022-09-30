package comiam.chat.client.security;

public class CryptUtil
{
    public static String encrypt(String msg)
    {
        try
        {
            return AESCipher.encryptMsg(msg, KeyDB.getKey());
        }catch (Throwable e)
        {
            return "error encrypt";
        }
    }
}
