package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Accessory;
import comiam.factoryapp.factory.components.Bodywork;
import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.components.Engine;
import comiam.factoryapp.factory.events.EventManager;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Store<T>
{
    private final Queue<T> components;
    private final EventManager eventManager;
    private final int limit;

    public Store(EventManager manager, int limit)
    {
        this.components = new ArrayBlockingQueue<>(limit);
        this.limit = limit;
        this.eventManager = manager;
    }

    public synchronized void putComponent(T obj)
    {
        while(isFull())
            try
            {
                this.wait();
            }catch(Throwable ignored)
            {
                Thread.currentThread().interrupt();
                return;
            }

        if(obj instanceof Accessory)
            eventManager.fireEvent(EventManager.ACCESSORY_DELIVERED_EVENT, null);
        else if(obj instanceof Engine)
            eventManager.fireEvent(EventManager.ENGINE_DELIVERED_EVENT, null);
        else if(obj instanceof Bodywork)
            eventManager.fireEvent(EventManager.BODYWORK_DELIVERED_EVENT, null);
        else if(obj instanceof Car)
            eventManager.fireEvent(EventManager.CAR_SUPPLIED_TO_STORE_EVENT, obj);

        components.offer(obj);

        this.notify();
    }

    public synchronized T getComponent()
    {
        while(components.size() == 0)
            try
            {
                this.wait();
            }catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return null;
            }

        T obj = components.poll();
        eventManager.fireEvent(EventManager.COMPONENT_SEND_FROM_STORE, obj);
        this.notifyAll();

        return obj;
    }

    private synchronized boolean isFull()
    {
        return components.size() == limit;
    }

    public synchronized int getLimit()
    {
        return limit;
    }

    public synchronized int getCurrentCount()
    {
        return components.size();
    }
}
