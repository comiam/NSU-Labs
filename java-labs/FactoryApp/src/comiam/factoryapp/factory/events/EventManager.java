package comiam.factoryapp.factory.events;

import java.util.HashMap;

public class EventManager
{
    public static final long ENGINE_DELIVERED_EVENT = 0;
    public static final long ACCESSORY_DELIVERED_EVENT = 1;
    public static final long BODYWORK_DELIVERED_EVENT = 2;
    public static final long PRODUCER_STARTED_DO_JOB_EVENT = 3;
    public static final long PRODUCER_DID_JOB_EVENT = 4;
    public static final long CAR_MADE_EVENT = 5;
    public static final long CAR_SUPPLIED_TO_STORE_EVENT = 6;
    public static final long CAR_SEND_EVENT = 7;
    public static final long COMPONENT_SEND_FROM_STORE = 8;

    private boolean enablePerformingEvents = true;
    private final HashMap<Long, EventHandler> eventHandlers;

    public EventManager()
    {
        this.eventHandlers = new HashMap<>();
    }

    public synchronized void setEventHandler(long ID, EventHandler handler)
    {
        eventHandlers.put(ID, handler);
    }

    public synchronized void fireEvent(long ID, Object object)
    {
        if(Thread.currentThread().isInterrupted())
            return;

        if(eventHandlers.containsKey(ID) && enablePerformingEvents)
            eventHandlers.get(ID).perform(object);
    }

    public synchronized boolean isEnablePerformingEvents()
    {
        return enablePerformingEvents;
    }

    public synchronized void disablePerformingEvents()
    {
        this.enablePerformingEvents = false;
    }

    public synchronized void enablePerformingEvents()
    {
        this.enablePerformingEvents = true;
    }
}
