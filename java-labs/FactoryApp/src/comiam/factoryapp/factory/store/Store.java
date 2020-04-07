package comiam.factoryapp.factory.store;

import java.util.LinkedList;
import java.util.Queue;

public class Store<T>
{
    private final Queue<T> components;
    private final int limit;

    public Store(int limit)
    {
        this.components = new LinkedList<>();
        this.limit = limit;
    }

    public synchronized void putComponent(T obj) throws InterruptedException
    {
        while(isFull())
            this.wait();

        this.notify();

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
