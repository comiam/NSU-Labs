package server;

import java.util.HashMap;

public class DataBundle
{
    private static final HashMap<Long, Long> totalCounters = new HashMap<>();
    private static long totalBytesReceived = 0;

    public static void addNewCounter(long id)
    {
        synchronized (totalCounters)
        {
            totalCounters.put(id, 0L);
        }
    }

    public static long getTotalBytesReceived(long id)
    {
        synchronized (totalCounters)
        {
            return totalCounters.get(id);
        }
    }

    public static void addToTotal(long id, long addition)
    {
        synchronized (totalCounters)
        {
            totalCounters.put(id, totalCounters.get(id) + addition);
        }
    }

    public static void removeCounter(long id)
    {
        synchronized (totalCounters)
        {
            totalCounters.remove(id);
        }
    }

    public static void clearAll()
    {
        synchronized (totalCounters)
        {
            totalCounters.clear();
        }
    }
}
