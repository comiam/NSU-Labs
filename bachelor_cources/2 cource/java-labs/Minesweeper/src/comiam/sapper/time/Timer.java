package comiam.sapper.time;

import java.util.TimerTask;

public class Timer
{
    private static long seconds = 0;
    private static int milliseconds = 0;
    private static Runnable runnable;
    private static TimerTask task;
    private static java.util.Timer timer;
    private static boolean running = false;

    private static void makeTask()
    {
        task = new TimerTask()
        {
            @Override
            public void run()
            {
                milliseconds++;
                seconds += milliseconds / 10;
                if(seconds + milliseconds / 10 > seconds)
                    runnable.run();
                milliseconds %= 10;
            }
        };
        timer = new java.util.Timer();
    }

    public static void start(Runnable r)
    {
        if(running)
            return;

        milliseconds = 0;
        seconds = 0;
        runnable = r;
        running = true;
        makeTask();
        schedule();
    }

    public static void stop()
    {
        if(timer == null || !running)
            return;
        running = false;
        timer.cancel();
        timer.purge();
    }

    public static void on()
    {
        if(running)
            return;
        running = true;
        makeTask();
        schedule();
    }

    public static void clearTime()
    {
        milliseconds = 0;
        seconds = 0;
    }

    public static boolean isRunning()
    {
        return running;
    }

    public static long getSeconds()
    {
        return seconds;
    }

    private static void schedule()
    {
        timer.schedule(task,0,100);
    }
}
