package comiam.factoryapp.gui.uicore;

import java.util.Arrays;

public class UIDataBundle
{
    public static final int MAX_LOG_SIZE = 26;

    private static Object[] logStack;
    private static double factoryWorkingProducers;
    private static long carMadeCount;
    private static long carSendCount;

    private static long engineDelivered;
    private static long bodyworkDelivered;
    private static long accessoryDelivered;

    public static synchronized void initDealerLogDataBundle(int dealerSize)
    {
        logStack = new Object[dealerSize];
    }

    public static synchronized void addLogData(int ID, Object... arr)
    {
        if(logStack.length == MAX_LOG_SIZE)
            return;

        logStack[ID] = arr;
    }

    public static synchronized void incFactoryWorkingProducers()
    {
        UIDataBundle.factoryWorkingProducers++;
    }

    public static synchronized void decFactoryWorkingProducers()
    {
        UIDataBundle.factoryWorkingProducers--;
    }

    public static synchronized void incCarMadeCount()
    {
        UIDataBundle.carMadeCount++;
    }

    public static synchronized void incCarSendCount()
    {

        UIDataBundle.carSendCount++;

    }

    public static synchronized void incEngineDelivered()
    {
        UIDataBundle.engineDelivered++;
    }

    public static synchronized void incBodyworkDelivered()
    {
        UIDataBundle.bodyworkDelivered++;
    }

    public static synchronized void incAccessoryDelivered()
    {
        UIDataBundle.accessoryDelivered++;
    }

    public static synchronized double getFactoryWorkingProducers()
    {
        return factoryWorkingProducers;
    }

    public static synchronized long getCarMadeCount()
    {
        return carMadeCount;
    }

    public static synchronized long getCarSendCount()
    {
        return carSendCount;
    }

    public static synchronized long getEngineDelivered()
    {
        return engineDelivered;
    }

    public static synchronized long getBodyworkDelivered()
    {
        return bodyworkDelivered;
    }

    public static synchronized long getAccessoryDelivered()
    {
        return accessoryDelivered;
    }

    public static synchronized Object getDealerLodData(int ID)
    {
        Object obj = logStack[ID];
        logStack[ID] = null;
        return obj;
    }

    public static synchronized void resetAll()
    {
        factoryWorkingProducers = 0;
        carSendCount = 0;
        carMadeCount = 0;
        accessoryDelivered = 0;
        bodyworkDelivered = 0;
        engineDelivered = 0;
        if(logStack != null)
            Arrays.fill(logStack, null);
    }
}
