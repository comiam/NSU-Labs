package comiam.chat.client.utils;

import java.lang.reflect.Array;

public class ByteUtils
{
    public static byte[] createPackage(String msg)
    {
        return concatenate(intToByteArray(msg.getBytes().length), msg.getBytes());
    }

    public static byte[] reverse(byte[] arr)
    {
        int start = 0;
        int end = arr.length - 1;
        while (start < end)
        {
            byte temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
        return arr;
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

    public static int byteArrayToInt(byte[] b)
    {
        int MASK = 0xFF;
        int result = b[0] & MASK;
        result = result + ((b[1] & MASK) << 8);
        result = result + ((b[2] & MASK) << 16);
        result = result + ((b[3] & MASK) << 24);
        return result;
    }

    public static byte[] intToByteArray(int value)
    {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}
