package socksproxy.auth;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

class PasswordCipher
{
    private final SecretKey secretKey;
    private final Base64.Encoder encoder;
    private final Base64.Decoder decoder;

    public PasswordCipher(String key)
    {
        secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        encoder = Base64.getEncoder();
        decoder = Base64.getDecoder();
    }

    public String encrypt(String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {

        byte[] plainTextByte = plainText.getBytes();

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);

        return encoder.encodeToString(encryptedByte);
    }

    public String decrypt(String encrypted) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
        byte[] encryptedByte = decoder.decode(encrypted);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte = cipher.doFinal(encryptedByte);

        return new String(decryptedByte);
    }
}
