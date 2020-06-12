package comiam.chat.server.utils;

public class ArgChecker
{
    public static boolean isNotNumeric(String str)
    {
        try
        {
            Integer.parseInt(str);
            return false;
        } catch(NumberFormatException e)
        {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean isBoolean(String str)
    {
        return str.equals("true") || str.equals("false");
    }
}

