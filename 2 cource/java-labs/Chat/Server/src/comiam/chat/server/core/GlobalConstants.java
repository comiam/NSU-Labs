package comiam.chat.server.core;

import comiam.chat.server.time.Timer;

public class GlobalConstants
{
    public static final int DEFAULT_TIMEOUT_IN_MINUTES = 5;
    public static final int MAX_MESSAGE_SIZE           = 2044;
    public static final int AUTHENTICATION_TIMEOUT     = Timer.SECOND * 30;
}
