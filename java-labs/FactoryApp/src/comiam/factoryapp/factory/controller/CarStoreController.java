package comiam.factoryapp.factory.controller;

import comiam.factoryapp.factory.producer.threadpool.TaskPool;
import comiam.factoryapp.factory.store.CarStore;

public class CarStoreController extends Thread
{
    public CarStoreController(TaskPool pool, CarStore store, int priority)
    {
        super(new ControllerRunnable(store, pool), "CarStoreController");
        setPriority(priority);
    }


}
