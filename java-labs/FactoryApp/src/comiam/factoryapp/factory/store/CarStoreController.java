package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.factory.producer.threadpool.FactoryTask;

public class CarStoreController extends Thread
{
    public CarStoreController(Factory factory)
    {
        super(() ->
        {
            while(!Thread.currentThread().isInterrupted())
            {
                synchronized(factory.getCarStore())
                {
                    if(factory.getProducerSection().getPool().getCountOfTaskJobs() < getCountOfMissingCars(factory))
                        synchronized(factory.getProducerSection())
                        {
                            factory.getProducerSection().getPool().pushTask(new FactoryTask(getCountOfMissingCars(factory) - factory.getProducerSection().getPool().getCountOfTaskJobs()));
                        }
                    else
                        try
                        {
                            factory.getCarStore().wait();
                        } catch(InterruptedException ignored) {}
                }
            }
        });
    }

    private static synchronized int getCountOfMissingCars(Factory factory)
    {
        return factory.getCarStore().getLimit() - factory.getCarStore().getCurrentCount();
    }
}
