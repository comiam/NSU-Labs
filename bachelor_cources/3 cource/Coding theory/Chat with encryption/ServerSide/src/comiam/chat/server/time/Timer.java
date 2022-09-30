package comiam.chat.server.time;

import comiam.chat.server.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

public class Timer
{
    private static long eventID = 0;

    public static final int MILLISECOND = 1;
    public static final int SECOND      = 1000 * MILLISECOND;
    public static final int MINUTE      = 60 * SECOND;
    public static final int HOUR        = 60 * MINUTE;

    public static final int HOURS_TYPE = 1;
    public static final int MINUTES_TYPE = 2;
    public static final int SECONDS_TYPE = 4;
    public static final int MILLISECONDS_TYPE = 8;
    public static final int ALL_PARAMETERS = HOURS_TYPE | MINUTES_TYPE | SECONDS_TYPE | MILLISECONDS_TYPE;

    private static long seconds = 0;
    private static int milliseconds = 0;
    private static final HashMap<Long, Pair<Long, Task>> timeEventSet = new HashMap<>();
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

                synchronized(timeEventSet)
                {
                    ArrayList<Long> trashBox = new ArrayList<>();

                    for(Long key : timeEventSet.keySet())
                    {
                        var pair = timeEventSet.get(key);
                        try
                        {
                            if((milliseconds + seconds * 1000) % pair.getFirst() == 0)
                                if(!pair.getSecond().run())
                                    trashBox.add(key);
                        }catch(Throwable ignored){}
                    }

                    for (Long key : trashBox)
                        timeEventSet.remove(key);
                }

                milliseconds %= 1000;
            }
        };
        timer = new java.util.Timer();
    }

    public static synchronized void unsubscribeEvent(long id)
    {
        synchronized(timeEventSet)
        {
            timeEventSet.remove(id);
        }
    }

    public static synchronized long subscribeEvent(Task runnable, long period)
    {
        synchronized(timeEventSet)
        {
            timeEventSet.put(eventID++, new Pair<>(period, runnable));
            return eventID - 1;
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

        if((parameters & HOURS_TYPE) > 0)
            time += getTimeString(getSeconds() / 3600, 2);

        if((parameters & MINUTES_TYPE) > 0)
            time += (time.isEmpty() ? "" : ":") + getTimeString(getSeconds() / 60, 2);

        if((parameters & SECONDS_TYPE) > 0)
            time += (time.isEmpty() ? "" : ":") + getTimeString(getSeconds() % 60, 2);

        if((parameters & MILLISECONDS_TYPE) > 0)
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
