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
            try
            {
                pool.getTask().getCarJob();

                if(pool.getTask().isDone())
                    pool.removeFirstTask();

                Thread.sleep(factory.getProducerDelay());

                engine = factory.getEngineStore().getComponent();
                accessory = factory.getAccessoryStore().getComponent();
                bodywork = factory.getBodyworkStore().getComponent();

                car = new Car(IDProduct.getID(), engine, bodywork, accessory);
                factory.getCarStore().putComponent(car);
            } catch(InterruptedException ignored) {}
        }
    }
}
