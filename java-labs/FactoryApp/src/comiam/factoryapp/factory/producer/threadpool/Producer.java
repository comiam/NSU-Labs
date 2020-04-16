package comiam.factoryapp.factory.producer.threadpool;

import comiam.factoryapp.factory.components.*;
import comiam.factoryapp.factory.events.EventManager;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.util.Bundle;

import static comiam.factoryapp.util.ThreadChecker.assertThreadInterrupted;

public class Producer implements Runnable
{
    private final TaskPool pool;
    private final Factory factory;
    private final Bundle<Integer> delay;

    public Producer(TaskPool pool, Factory factory)
    {
        this.factory = factory;
        this.pool = pool;
        this.delay = factory.getProducerDelay();
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
                //need for wake up the store controller, if it necessary decide to sleep
                synchronized(factory.getCarStore())
                {
                    factory.getCarStore().notifyAll();
                }
                pool.getTask().getCarJob();

                if(pool.getTask().isDone())
                    pool.removeFirstTask();

                engine = factory.getEngineStore().getComponent();
                assertThreadInterrupted();

                accessory = factory.getAccessoryStore().getComponent();
                assertThreadInterrupted();

                bodywork = factory.getBodyworkStore().getComponent();
                assertThreadInterrupted();

                factory.getEventManager().fireEvent(EventManager.PRODUCER_STARTED_DO_JOB_EVENT, null);

                if(delay.getVal() != 0)
                    Thread.sleep(delay.getVal());

                car = new Car(IDProduct.getID(), engine, bodywork, accessory);
                factory.getCarStore().putComponent(car);
                assertThreadInterrupted();

                factory.getEventManager().fireEvent(EventManager.PRODUCER_DID_JOB_EVENT, null);
                factory.getEventManager().fireEvent(EventManager.CAR_MADE_EVENT, car);
            } catch(Throwable ignored) {
                break;
            }
        }
    }
}
