package comiam.chat.client.time;

import java.text.SimpleDateFormat;

public class Date
{
    public static String getDate()
    {
        return new SimpleDateFormat("dd.MM.yyyy_hh:mm").format(new java.util.Date());
    }
}
