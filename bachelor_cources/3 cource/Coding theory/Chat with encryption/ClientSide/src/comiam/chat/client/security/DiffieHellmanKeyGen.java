package comiam.chat.client.security;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiffieHellmanKeyGen
{
    private static final int BIT_LENGTH = 512;
    private static final int CERTAINTY = 20;

    private static final SecureRandom rnd = new SecureRandom();

    public static BigInteger getGeneratedKey(BigInteger publicValue, BigInteger prime, BigInteger secretValue)
    {
        return publicValue.modPow(secretValue, prime);
    }

    public static BigInteger getPublicValue(BigInteger generator, BigInteger prime, BigInteger secretValue)
    {
        return generator.modPow(secretValue, prime);
    }

    public static BigInteger getSecretValue()
    {
        Random randomGenerator = new Random();
        return new BigInteger(BIT_LENGTH - 2, randomGenerator);
    }

    public static BigInteger getPrime()
    {
        Random rnd = new Random();
        return new BigInteger(BIT_LENGTH, CERTAINTY, rnd);
    }

    public static BigInteger getGenerator(BigInteger prime)
    {
        int start = 2001;//One of best precalculated begins

        for (int i = start; i < 100000000; i++)
            if (isPrimeRoot(BigInteger.valueOf(i), prime))
                return BigInteger.valueOf(i);

        return BigInteger.valueOf(0);
    }

    private static boolean isPrimeRoot(BigInteger g, BigInteger p)
    {
        BigInteger totient = p.subtract(BigInteger.ONE);
        List<BigInteger> factors = primeFactors(totient);
        int i = 0;
        int j = factors.size();
        for (; i < j; i++)
        {
            BigInteger factor = factors.get(i);
            BigInteger t = totient.divide(factor);
            if (g.modPow(t, p).equals(BigInteger.ONE))
                return false;
        }
        return true;
    }

    private static List<BigInteger> primeFactors(BigInteger number)
    {
        BigInteger n = number;
        BigInteger i = BigInteger.valueOf(2);
        BigInteger limit = BigInteger.valueOf(10000);
        List<BigInteger> factors = new ArrayList<>();
        while (!n.equals(BigInteger.ONE))
        {
            while (n.mod(i).equals(BigInteger.ZERO))
            {
                factors.add(i);
                n = n.divide(i);

                if (isPrime(n))
                {
                    factors.add(n);
                    return factors;
                }
            }
            i = i.add(BigInteger.ONE);
            if (i.equals(limit))
                return factors;
        }
        System.out.println(factors);
        return factors;
    }

    private static boolean millerRabinPass(BigInteger a, BigInteger n)
    {
        BigInteger nMinusOne = n.subtract(BigInteger.ONE);
        BigInteger d = nMinusOne;
        int s = d.getLowestSetBit();
        d = d.shiftRight(s);
        BigInteger aToPower = a.modPow(d, n);

        if (aToPower.equals(BigInteger.ONE))
            return true;

        for (int i = 0; i < s - 1; i++)
        {
            if (aToPower.equals(nMinusOne))
                return true;
            aToPower = aToPower.multiply(aToPower).mod(n);
        }

        return aToPower.equals(nMinusOne);
    }

    private static boolean millerRabin(BigInteger n)
    {
        for (int repeat = 0; repeat < 20; repeat++)
        {
            BigInteger a;
            do
            {
                a = new BigInteger(n.bitLength(), rnd);
            } while (a.equals(BigInteger.ZERO));
            if (!millerRabinPass(a, n))
                return false;
        }
        return true;
    }

    private static boolean isPrime(BigInteger r)
    {
        return millerRabin(r);
    }
}
