package comiam.factoryapp.factory.producer.threadpool;

import comiam.factoryapp.factory.components.*;
import comiam.factoryapp.factory.factory.Factory;

public class Producer implements Runnable
{
    private final TaskPool pool;
    private final Factory factory;

    public Producer(Factory factory, TaskPool pool)
    {
        this.factory = factory;
        this.pool = pool;
    }


    @Override
    public void run()
    {
        Engine engine;
        Accessory accessory;
        Bodywork bodywork;
        Car car;

        while(!Thread.currentThread().isInterrupted())
        {
            synchronized(pool)
            {
                while(pool.isEmpty())
                    try
                    {
                        pool.wait();
                    } catch(InterruptedException ignored) {}

                pool.getTask().getJob();

                if(pool.getTask().isDone())
                    pool.removeFirstTask();
            }

            try
            {
                Thread.sleep(factory.getProducerDelay());
            } catch(InterruptedException ignored) {}

            synchronized(factory.getEngineStore())
            {
                while(factory.getEngineStore().isEmpty())
                    try
                    {
                        factory.getEngineStore().wait();
                    } catch(InterruptedException ignored) {}

                engine = factory.getEngineStore().getComponent();
            }

            synchronized(factory.getAccessoryStore())
            {
                while(factory.getAccessoryStore().isEmpty())
                    try
                    {
                        factory.getAccessoryStore().wait();
                    } catch(InterruptedException ignored) {}

                accessory = factory.getAccessoryStore().getComponent();
            }

            synchronized(factory.getBodyworkStore())
            {
                while(factory.getBodyworkStore().isEmpty())
                    try
                    {
                        factory.getBodyworkStore().wait();
                    } catch(InterruptedException ignored) {}

                bodywork = factory.getBodyworkStore().getComponent();
            }

            car = new Car(IDProduct.getID(), engine, bodywork, accessory);

            synchronized(factory.getCarStore())
            {
                while(factory.getCarStore().isFull())
                    try
                    {
                        factory.getCarStore().wait();
                    } catch(InterruptedException ignored) {}

                try
                {
                    factory.getCarStore().putComponent(car);
                } catch(InterruptedException ignored) {}
            }
        }
    }
}
