package comiam.sapper.util;

public class TextUtils
{
    public static boolean isNumeric(String str)
    {
        try
        {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e)
        {
            return false;
        }
    }

    public static <T extends Number> String getNumericString(T val)
    {
        if(val == null || val.longValue() > 100 || val.longValue() < 0)
            return "  ";

        String str = Long.toString(val.longValue());
        if(str.length() == 1)
            str = " " + str;

        return str;
    }

    public static String getTimeString(long val)
    {
        String time = Long.toString(val);
        if(time.length() == 1)
            time = "0" + time;

        return time;
    }
}
