package comiam.factoryapp.factory.store;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Store<T>
{
    private final BlockingQueue<T> components;
    private final int limit;

    public Store(int limit)
    {
        this.components = new ArrayBlockingQueue<>(limit);
        this.limit = limit;
    }

    public synchronized void putComponent(T obj) throws InterruptedException
    {
        if(isFull())
            return;
        components.put(obj);
        notifyAll();
    }

    public synchronized T getComponent()
    {
        if(components.size() == 0)
            return null;

        T component = components.poll();
        notifyAll();

        return component;
    }

    public synchronized boolean isEmpty()
    {
        return components.size() == 0;
    }

    public synchronized boolean isFull()
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
