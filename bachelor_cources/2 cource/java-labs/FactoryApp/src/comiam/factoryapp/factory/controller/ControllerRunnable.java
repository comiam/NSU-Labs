package comiam.factoryapp.factory.controller;

import comiam.factoryapp.factory.producer.threadpool.FactoryTask;
import comiam.factoryapp.factory.producer.threadpool.TaskPool;
import comiam.factoryapp.factory.store.CarStore;

public class ControllerRunnable implements Runnable
{
    private final TaskPool pool;
    private final CarStore store;

    public ControllerRunnable(CarStore store, TaskPool pool)
    {
        this.pool = pool;
        this.store = store;
    }

    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                if(pool.getCountOfTaskJobs() < getCountOfMissingCars())
                    pool.pushTask(new FactoryTask(getCountOfMissingCars() - pool.getCountOfTaskJobs()));
                else
                    synchronized(store)
                    {
                        if(pool.getCountOfTaskJobs() >= getCountOfMissingCars())
                            store.wait();
                    }
            }catch(Throwable ignored) {
                break;
            }
        }
    }

    private int getCountOfMissingCars()
    {
        return store.getLimit() - store.getCurrentCount();
    }
}
