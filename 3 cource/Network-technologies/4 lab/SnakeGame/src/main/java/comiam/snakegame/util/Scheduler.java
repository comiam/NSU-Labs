package comiam.snakegame.util;

import comiam.snakegame.util.unsafe.UnsafeRunnable;
import lombok.val;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@FunctionalInterface
public interface Scheduler
{

    TimerTask schedule(
            final UnsafeRunnable executable,
            final int periodMs);

    static Scheduler fromTimer(final Timer timer)
    {
        return (executable, period) -> {
            Logger.getLogger(Scheduler.class.getSimpleName()).info("Task scheduled");
            val task = new TimerTask()
            {

                @Override
                public void run()
                {
                    try
                    {
                        executable.run();
                    } catch (final Exception e)
                    {
                        Logger.getLogger(Scheduler.class.getSimpleName()).warning(
                                "Executable thrown an exception " + e.getClass().getSimpleName()
                                        + ": " + e.getMessage());
                        e.printStackTrace();
                        timer.cancel();
                    }
                }

                @Override
                public boolean cancel()
                {
                    Logger.getLogger(Scheduler.class.getSimpleName()).info("Task cancelled");
                    return super.cancel();
                }
            };
            timer.scheduleAtFixedRate(task, period, period);
            return task;
        };
    }
}


