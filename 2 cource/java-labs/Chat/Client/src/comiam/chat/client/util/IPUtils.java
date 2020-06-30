package comiam.chat.client.util;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IPUtils
{
    private static Pattern VALID_IPV4_PATTERN = null;
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

    static {
        try
        {
            VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ignored) {}
    }

    public static boolean isIpAddress(String ipAddress)
    {
        return VALID_IPV4_PATTERN.matcher(ipAddress).matches();
    }
}
