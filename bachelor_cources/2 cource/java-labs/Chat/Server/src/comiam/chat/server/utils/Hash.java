package comiam.chat.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
    Murmur3 hash algorithm
 */
public final class Hash
{
    private static final long X64_128_C1 = 0x87C37B91114253D5L;
    private static final long X64_128_C2 = 0x4Cf5AD432745937fL;
    private static final long X86_128_C1 = 0x85EBCA6B;
    private static final long X86_128_C2 = 0xC2B2AE35;
    private static final int X64_128_SEED = 0xA115FECD;
    private static final int X86_128_SEED = 0xD89AF55A;

    public static String hashBytes(byte[] bytes)
    {
        int arch = System.getProperty("os.arch").toLowerCase().contains("64") ? 64 : 32;
        if (arch == 64)
            return hash_x64_128(bytes, bytes.length, X64_128_SEED, true);
        else
            return hash_x86_128(bytes, bytes.length, X86_128_SEED, true);
    }

    public static String hashInputStream(InputStream is) throws IOException
    {
        byte[] buffer = new byte[1024000];
        int len;
        boolean first = false;
        StringBuilder result = null;

        int arch = System.getProperty("os.arch").toLowerCase().contains("64") ? 64 : 32;
        if (arch == 64)
        {
            while ((len = is.read(buffer)) != -1)
            {
                if (!first)
                {
                    result = new StringBuilder(hash_x64_128(buffer, len, X64_128_SEED, false));
                    first = true;
                } else
                {
                    result.append(hash_x64_128(buffer, len, X64_128_SEED, false));
                }
            }
            assert result != null;
            return hash_x64_128(result.toString().getBytes(), result.toString().getBytes().length, X64_128_SEED, true);
        } else
        {
            while ((len = is.read(buffer)) != -1)
            {
                if (!first)
                {
                    result = new StringBuilder(hash_x86_128(buffer, len, X86_128_SEED, false));
                    first = true;
                } else
                {
                    result.append(hash_x86_128(buffer, len, X86_128_SEED, false));
                }
            }
            assert result != null;
            return hash_x86_128(result.toString().getBytes(), result.toString().getBytes().length, X86_128_SEED, true);
        }
    }

    public static String hashFile(String path) throws IOException
    {
        return hashInputStream(new FileInputStream(new File(path)));
    }

    public static String hash_x64_128(final byte[] data, final int length, final long seed, boolean tohex)
    {
        long h1 = seed & 0x00000000FFFFFFFFL;
        long h2 = seed & 0x00000000FFFFFFFFL;

        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        int roundedEnd = (length & 0xFFFFFFF0);  // round down to 16 byte block
        for (int i = 0; i < roundedEnd; i += 16)
        {
            // little endian load order
            long k1 = getLongLittleEndian(data, i);
            long k2 = getLongLittleEndian(data, i + 8);
            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;
            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }
        //tail

        long k1 = 0;
        long k2 = 0;

        switch (length & 15)
        {
            case 15:
                k2 = (data[roundedEnd + 14] & 0xffL) << 48;
            case 14:
                k2 |= (data[roundedEnd + 13] & 0xffL) << 40;
            case 13:
                k2 |= (data[roundedEnd + 12] & 0xffL) << 32;
            case 12:
                k2 |= (data[roundedEnd + 11] & 0xffL) << 24;
            case 11:
                k2 |= (data[roundedEnd + 10] & 0xffL) << 16;
            case 10:
                k2 |= (data[roundedEnd + 9] & 0xffL) << 8;
            case 9:
                k2 |= (data[roundedEnd + 8] & 0xffL);
                k2 *= c2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= c1;
                h2 ^= k2;
            case 8:
                k1 = ((long) data[roundedEnd + 7]) << 56;
            case 7:
                k1 |= (data[roundedEnd + 6] & 0xffL) << 48;
            case 6:
                k1 |= (data[roundedEnd + 5] & 0xffL) << 40;
            case 5:
                k1 |= (data[roundedEnd + 4] & 0xffL) << 32;
            case 4:
                k1 |= (data[roundedEnd + 3] & 0xffL) << 24;
            case 3:
                k1 |= (data[roundedEnd + 2] & 0xffL) << 16;
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xffL) << 8;
            case 1:
                k1 |= (data[roundedEnd] & 0xffL);
                k1 *= c1;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= c2;
                h1 ^= k1;
        }

        // finalization

        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        // fmix
        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return tohex ? (Long.toHexString(h1) + "" + Long.toHexString(h2)).replaceAll("-", "") :
                (h1 + "" + h2).replaceAll("-", "");
    }

    public static String hash_x86_128(byte[] data, int length, int seed, boolean toHex)
    {
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;
        final int c3 = 0x38b34ae5;
        final int c4 = 0xa1e38b93;

        int h1 = seed & 0x0000FFFF;
        int h2 = seed & 0x0000FFFF;
        int h3 = seed & 0x0000FFFF;
        int h4 = seed & 0x0000FFFF;

        int roundedEnd = (length & 0xFFFFFFF0);

        for (int i = 0; i < roundedEnd; i += 16) // round down to 16 byte block
        {
            // little endian load order
            int k1 = getIntLittleEndian(data, i);
            int k2 = getIntLittleEndian(data, i + 2);
            int k3 = getIntLittleEndian(data, i + 4);
            int k4 = getIntLittleEndian(data, i + 8);

            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);
            k1 *= c2;

            h1 ^= k1;
            h1 = (h1 << 13) | (h1 >>> 19);
            h1 = h1 * 5 + 0xe6546b64;

            k2 *= c2;
            k2 = (k2 << 16) | (k2 >>> 16);
            k2 *= c3;
            h2 ^= k2;

            h2 = (h2 << 17) | (h2 >>> 15);
            h2 += h3;
            h2 = h2 * 5 + 0x0bcaa747;

            k3 *= c3;
            k3 = (k3 << 17) | (k3 >>> 15);
            k3 *= c4;
            h3 ^= k3;

            h3 = (h3 << 15) | (h3 >>> 17);
            h3 += h4;
            h3 = h3 * 5 + 0x96cd1c35;

            k4 *= c4;
            k4 = (k4 << 18) | (k4 >>> 14);
            k4 *= c1;
            h4 ^= k4;

            h4 = (h4 << 13) | (h4 >>> 19);
            h4 += h1;
            h4 = h4 * 5 + 0x32ac3b17;
        }

        // tail
        int k1 = 0;
        int k2 = 0;
        int k3 = 0;
        int k4 = 0;

        switch (length & 0x15)
        {
            case 15:
                k4 ^= (data[roundedEnd + 14] & 0xff) << 16;
            case 14:
                k4 ^= (data[roundedEnd + 13] & 0xff) << 8;
            case 13:
                k4 ^= (data[roundedEnd + 12] & 0xff);
                k4 *= c4;
                k4 = (k4 << 18) | (k4 >>> 14);
                k4 *= c1;
                h4 ^= k4;

            case 12:
                k3 ^= (data[roundedEnd + 11] & 0xff) << 24;
            case 11:
                k3 ^= (data[roundedEnd + 10] & 0xff) << 16;
            case 10:
                k3 ^= (data[roundedEnd + 9] & 0xff) << 8;
            case 9:
                k3 ^= (data[roundedEnd + 8] & 0xff) << 0;
                k3 *= c3;
                k3 = (k3 << 17) | (k3 >>> 15);
                k3 *= c4;
                h3 ^= k3;

            case 8:
                k2 ^= (data[roundedEnd + 7] & 0xff) << 24;
            case 7:
                k2 ^= (data[roundedEnd + 6] & 0xff) << 16;
            case 6:
                k2 ^= (data[roundedEnd + 5] & 0xff) << 8;
            case 5:
                k2 ^= (data[roundedEnd + 4] & 0xff) << 0;
                k2 *= c2;
                k2 = (k2 << 16) | (k2 >>> 16);
                k2 *= c3;
                h2 ^= k2;

            case 4:
                k1 ^= (data[roundedEnd + 3] & 0xff) << 24;
            case 3:
                k1 ^= (data[roundedEnd + 2] & 0xff) << 16;
            case 2:
                k1 ^= (data[roundedEnd + 1] & 0xff) << 8;
            case 1:
                k1 ^= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);
                k1 *= c2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= length;
        h2 ^= length;
        h3 ^= length;
        h4 ^= length;

        h1 += h2;
        h1 += h3;
        h1 += h4;
        h2 += h1;
        h3 += h1;
        h4 += h1;

        // fmix;
        h1 = fmix32(h1);
        h2 = fmix32(h2);
        h3 = fmix32(h3);
        h4 = fmix32(h4);

        h1 += h2;
        h1 += h3;
        h1 += h4;
        h2 += h1;
        h3 += h1;
        h4 += h1;

        return toHex ? (Integer.toHexString(h1) + Integer.toHexString(h2) + Integer.toHexString(h3) + Integer.toHexString(h4)).replaceAll("-", "") :
                (h1 + "" + h2 + "" + h3 + "" + h4).replaceAll("-", "");
    }

    private static long fmix64(long k)
    {
        k ^= k >>> 33;
        k *= X64_128_C1;
        k ^= k >>> 33;
        k *= X64_128_C2;
        k ^= k >>> 33;

        return k;
    }

    private static int fmix32(int h)
    {
        h ^= h >>> 16;
        h *= X86_128_C1;
        h ^= h >>> 13;
        h *= X86_128_C2;
        h ^= h >>> 16;

        return h;
    }

    private static int getIntLittleEndian(byte[] buf, int offset)
    {
        return buf[offset + 3] << 24
                | ((buf[offset + 2] & 0xff) << 16)
                | ((buf[offset + 1] & 0xff) << 8)
                | ((buf[offset] & 0xff));
    }

    private static long getLongLittleEndian(byte[] buf, int offset)
    {
        return ((long) buf[offset + 7] << 56)
                | ((buf[offset + 6] & 0xffL) << 48)
                | ((buf[offset + 5] & 0xffL) << 40)
                | ((buf[offset + 4] & 0xffL) << 32)
                | ((buf[offset + 3] & 0xffL) << 24)
                | ((buf[offset + 2] & 0xffL) << 16)
                | ((buf[offset + 1] & 0xffL) << 8)
                | ((buf[offset] & 0xffL));
    }
}
