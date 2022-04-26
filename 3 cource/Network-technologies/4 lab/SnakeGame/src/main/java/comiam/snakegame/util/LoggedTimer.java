package comiam.snakegame.util;

import java.util.Timer;
import java.util.logging.Logger;


public class LoggedTimer extends Timer
{

    private static final Logger logger = Logger.getLogger(LoggedTimer.class.getSimpleName());

    public LoggedTimer()
    {
        super();
    }

    @Override
    public void cancel()
    {
        logger.info(Thread.currentThread().getName() + " cancelled the timer");
        super.cancel();
    }
}
