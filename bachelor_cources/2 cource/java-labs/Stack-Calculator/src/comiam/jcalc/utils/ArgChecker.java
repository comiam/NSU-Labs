package comiam.jcalc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgChecker
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

    public static boolean containsSpecialSymbols(String arg)
    {
        Pattern special = Pattern.compile("[!@$%&()_=|<>?{}\\[\\]~]");
        Matcher hasSpecial = special.matcher(arg);

        return hasSpecial.find();
    }
}
