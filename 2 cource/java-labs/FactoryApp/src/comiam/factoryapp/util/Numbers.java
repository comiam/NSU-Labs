package comiam.factoryapp.util;

import java.util.function.Function;

public class Numbers
{
    public static boolean isNumeric(String str)
    {
        try
        {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e)
        {
            return false;
        }
    }

    public static Integer getIntegerWithCondition(String str, Function<Integer, Boolean> condition)
    {
        Integer num = getInteger(str);

        return num == null ? null : condition.apply(num) ? num : null;
    }

    public static Integer getInteger(String str)
    {
        if(!isNumeric(str) || str.isEmpty())
            return null;

        return Integer.parseInt(str);
    }
}
