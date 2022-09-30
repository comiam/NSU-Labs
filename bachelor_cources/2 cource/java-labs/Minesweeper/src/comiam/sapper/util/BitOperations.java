package comiam.sapper.util;

public class BitOperations
{
    public static int getBit(byte num, int position)
    {
        return (num >> position) & 1;
    }

    public static byte setBit(byte x, int value, int index)
    {
        if (value == 1)
            x |= 1 << index;
        else
            x &= ~(1 << index);

        return x;
    }
}
