package comiam.chat.server.time;

import java.text.SimpleDateFormat;

public class Date
{
    public static String getDate()
    {
        return new SimpleDateFormat("dd.MM.yyyy_hh:mm").format(new java.util.Date());
    }

    public static String getDateForSession()
    {
        return new SimpleDateFormat("dd.MM.yyyy_hh:mm::ss::SSSS").format(new java.util.Date());
    }
}
