package comiam.chat.client.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class AESCipher
{
    public static String encryptMsg(String message, BigInteger numKey) throws Throwable
    {
        SecretKeySpec secretKeySpec = getCipherKey(numKey);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] cipherText = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decryptMsg(String message, BigInteger numKey) throws Throwable
    {
        SecretKeySpec secretKeySpec = getCipherKey(numKey);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(message));

        return new String(plainText, StandardCharsets.UTF_8);
    }

    private static SecretKeySpec getCipherKey(BigInteger secret) throws Throwable
    {
        byte[] key = secret.toString().getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }
}
