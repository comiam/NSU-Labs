package comiam.factoryapp.time;

import java.util.ArrayList;
import java.util.TimerTask;

public class Timer
{
    private static class Pair
    {
        private final long period;
        private final Runnable action;

        public Pair(long period, Runnable action)
        {
            this.action = action;
            this.period = period;
        }

        public Runnable getAction()
        {
            return action;
        }

        public long getPeriod()
        {
            return period;
        }
    }

    public static final int HOURS = 1;
    public static final int MINUTES = 2;
    public static final int SECONDS = 4;
    public static final int MILLISECONDS = 8;
    public static final int ALL_PARAMETERS = HOURS | MINUTES | SECONDS | MILLISECONDS;

    private static long seconds = 0;
    private static int milliseconds = 0;
    private static final ArrayList<Pair> timeEventSet = new ArrayList<>();
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
                seconds += milliseconds / 1000;

                for(int i = 0; i < timeEventSet.size(); i++)
                {
                    var pair = timeEventSet.get(i);
                    try
                    {
                        if(milliseconds % pair.getPeriod() != 0)
                            pair.getAction().run();
                    }catch(Throwable ignored){}
                }

                milliseconds %= 1000;
            }
        };
        timer = new java.util.Timer();
    }

    public static synchronized void subscribeEvent(Runnable runnable, long period)
    {
        synchronized(timeEventSet)
        {
            timeEventSet.add(new Pair(period, runnable));
        }
    }

    public static synchronized void start()
    {
        if(running)
            return;

        milliseconds = 0;
        seconds = 0;
        running = true;
        makeTask();
        schedule();
    }

    public static synchronized void stop()
    {
        if(timer == null || !running)
            return;
        running = false;
        timer.cancel();
        timer.purge();
        synchronized(timeEventSet)
        {
            timeEventSet.clear();
        }
    }

    public static synchronized void on()
    {
        if(running)
            return;
        running = true;
        makeTask();
        schedule();
    }

    public static synchronized void clearTime()
    {
        milliseconds = 0;
        seconds = 0;
    }

    public static synchronized boolean isRunning()
    {
        return running;
    }

    public static synchronized long getSeconds()
    {
        return seconds;
    }

    public static synchronized long getMilliSeconds()
    {
        return milliseconds;
    }

    private static synchronized void schedule()
    {
        timer.schedule(task,0,1);
    }

    /**
     * Return time string
     *
     * @throws IllegalStateException if Timer not scheduled
     * @param parameters - bit mask of parameters, which put in string:
     *                      Timer.HOURS - put hours
     *                      Timer.MINUTES - put minutes
     *                      Timer.SECONDS - put seconds
     *                      Timer.MILLISECONDS - put milliseconds
     * @return string with set parameters
     */
    public static synchronized String getTime(int parameters)
    {
        if(!isRunning())
            throw new IllegalStateException("Timer not scheduled");

        if(parameters == 0)
            return "";

        String time = "";

        if((parameters & HOURS) > 0)
            time += getTimeString(getSeconds() / 3600, 2);

        if((parameters & MINUTES) > 0)
            time += (time.isEmpty() ? "" : ":") + getTimeString(getSeconds() / 60, 2);

        if((parameters & SECONDS) > 0)
            time += (time.isEmpty() ? "" : ":") + getTimeString(getSeconds() % 60, 2);

        if((parameters & MILLISECONDS) > 0)
            time += (time.isEmpty() ? "" : ":") + getTimeString(getMilliSeconds(), 3);

        return time;
    }

    private static String getTimeString(long val, int size)
    {
        StringBuilder time = new StringBuilder(Long.toString(val));
        int end = size - time.length();
        if(time.length() < size)
            for(int i = 0; i < end; i++)
                time.insert(0, "0");

        return time.toString();
    }
}
