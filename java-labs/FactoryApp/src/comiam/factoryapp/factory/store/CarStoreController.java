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
                try
                {
                    if(factory.getProducerSection().getPool().getCountOfTaskJobs() < getCountOfMissingCars(factory))
                        factory.getProducerSection().getPool().pushTask(new FactoryTask(getCountOfMissingCars(factory) - factory.getProducerSection().getPool().getCountOfTaskJobs()));
                    else
                        synchronized(factory.getCarStore())
                        {
                            factory.getCarStore().wait();
                        }
                }catch(Throwable ignored){
                    return;
                }
            }
        }, "CarStoreController");
    }

    private static int getCountOfMissingCars(Factory factory)
    {
        return factory.getCarStore().getLimit() - factory.getCarStore().getCurrentCount();
    }
}
