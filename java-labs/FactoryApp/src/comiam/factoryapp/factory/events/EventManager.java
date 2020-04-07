package comiam.factoryapp.factory.events;

import java.util.HashMap;
import java.util.Map;

public class EventManager
{
    public static final long ENGINE_SUPPLIED_EVENT = 0;
    public static final long ACCESSORY_SUPPLIED_EVENT = 1;
    public static final long BODYWORK_SUPPLIED_EVENT = 2;
    public static final long PRODUCER_STARTED_DO_JOB_EVENT = 3;
    public static final long PRODUCER_DID_JOB_EVENT = 4;
    public static final long CAR_MADE_EVENT = 5;
    public static final long CAR_SUPPLIED_TO_STORE_EVENT = 6;
    public static final long CAR_SEND_EVENT = 7;

    private final Map<Long, EventHandler> eventHandlers;

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
        if(eventHandlers.containsKey(ID))
            eventHandlers.get(ID).perform(object);
    }
}
