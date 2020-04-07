package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Accessory;
import comiam.factoryapp.factory.components.Bodywork;
import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.components.Engine;
import comiam.factoryapp.factory.events.EventManager;
import comiam.factoryapp.factory.factory.Factory;

import java.util.LinkedList;
import java.util.Queue;

public class Store<T>
{
    private final Queue<T> components;
    private final int limit;
    private final Factory factory;

    public Store(Factory factory, int limit)
    {
        this.components = new LinkedList<>();
        this.limit = limit;
        this.factory = factory;
    }

    public synchronized void putComponent(T obj) throws InterruptedException
    {
        while(isFull())
            this.wait();

        this.notify();
        if(obj instanceof Accessory)
            factory.getEventManager().fireEvent(EventManager.ACCESSORY_SUPPLIED_EVENT, null);
        else if(obj instanceof Engine)
            factory.getEventManager().fireEvent(EventManager.ENGINE_SUPPLIED_EVENT, null);
        else if(obj instanceof Bodywork)
            factory.getEventManager().fireEvent(EventManager.BODYWORK_SUPPLIED_EVENT, null);
        else if(obj instanceof Car)
            factory.getEventManager().fireEvent(EventManager.CAR_SUPPLIED_TO_STORE_EVENT, obj);

        components.offer(obj);
    }

    public synchronized T getComponent() throws InterruptedException
    {
        while(components.size() == 0)
            this.wait();

        this.notify();

        return components.poll();
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
