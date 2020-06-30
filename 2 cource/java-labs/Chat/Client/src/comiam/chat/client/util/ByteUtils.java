package comiam.chat.client.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

public class ByteUtils
{
    public static byte[] genMessageByteArray(String message)
    {
        return ByteUtils.concatenate(ByteUtils.intToByteArray(message.length()), message.getBytes());
    }

    public static byte[] concatenate(byte[] a, byte[] b)
    {
        int aLen = a.length;
        int bLen = b.length;

        byte[] c = (byte[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static byte[] intToByteArray(int value)
    {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static byte[] readAllFrom(InputStream is) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != 0)
            buffer.write(data, 0, nRead);

        return buffer.toByteArray();
    }
}
