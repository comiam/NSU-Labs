package comiam.factoryapp.util;

public class Strings
{
    public static String getTimeString(long val, int size)
    {
        StringBuilder time = new StringBuilder(Long.toString(val));
        int end = size - time.length();
        if(time.length() < size)
            for(int i = 0; i < end; i++)
                time.insert(0, "0");

        return time.toString();
    }
}
