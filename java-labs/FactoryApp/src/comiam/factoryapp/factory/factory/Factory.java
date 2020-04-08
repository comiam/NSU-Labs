package comiam.factoryapp.factory.factory;

import comiam.factoryapp.factory.dealer.Dealer;
import comiam.factoryapp.factory.events.EventHandler;
import comiam.factoryapp.factory.events.EventManager;
import comiam.factoryapp.factory.producer.ProducerSection;
import comiam.factoryapp.factory.store.*;
import comiam.factoryapp.factory.supplier.AccessorySupplier;
import comiam.factoryapp.factory.supplier.BodyworkSupplier;
import comiam.factoryapp.factory.supplier.EngineSupplier;
import comiam.factoryapp.log.Log;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Factory
{
    private boolean initialized = false;
    private EventManager eventManager;
    private ArrayList<Thread> threadPool;
    private CarStore carStore;
    private AccessoryStore accessoryStore;
    private BodyworkStore bodyworkStore;
    private EngineStore engineStore;
    private ProducerSection producerSection;
    private CarStoreController carStoreController;

    public Factory()
    {
        eventManager = new EventManager();
    }

    private int supplierCount = 0;
    private int producerCount = 0;
    private int dealerCount = 0;
    private int supplierDelay = 0;
    private int producerDelay = 0;
    private int dealerDelay = 0;

    /**
     * Destroy all threads in factory, factory will stop. All data will be deleted.
     */
    public void destroy()
    {
        stopThreads();
        threadPool.clear();

        threadPool = null;
        carStore = null;
        accessoryStore = null;
        bodyworkStore = null;
        engineStore = null;
        producerSection = null;
        carStoreController = null;
        eventManager = null;

        initialized = false;
        System.gc();
    }

    /**
     * Init all factory processes: Dealers, Producers and suppliers. If any delay equals -1, when this delay sets randomly from 0,2 to 1.
     *
     * @param accessorySupplierCount - count of suppliers for this factory.
     * @param producerCount - count of producers for this factory.
     * @param dealerCount - count of dealers for this factory.
     * @param supplierDelay - delay in milliseconds with which suppliers deliver components.
     * @param producerDelay - delay in milliseconds with which producers make cars.
     * @param dealerDelay - delay in milliseconds with which dealers buy cars.
     * @param accessoryStoreLimit - limit of components for accessory store
     * @param engineStoreLimit - limit of components for engine store
     * @param bodyworkStoreLimit - limit of components for bodywork store
     * @param carStoreLimit - limit of components for car store
     */
    public void init(int accessorySupplierCount, int producerCount, int dealerCount, int supplierDelay, int producerDelay, int dealerDelay,
                     int accessoryStoreLimit, int engineStoreLimit, int bodyworkStoreLimit, int carStoreLimit)
    {
        if(initialized)
            return;

        if(supplierDelay == -1)
            supplierDelay = (int)(randomizeDelay() * 1000);

        if(producerDelay == -1)
            supplierDelay = (int)(randomizeDelay() * 1000);

        if(dealerDelay == -1)
            supplierDelay = (int)(randomizeDelay() * 1000);

        this.supplierCount = accessorySupplierCount;
        this.dealerCount = dealerCount;
        this.producerCount = producerCount;
        this.supplierDelay = supplierDelay;
        this.producerDelay = producerDelay;
        this.dealerDelay = dealerDelay;

        producerSection = new ProducerSection(this);
        carStore = new CarStore(this, carStoreLimit);
        accessoryStore = new AccessoryStore(this, accessoryStoreLimit);
        bodyworkStore = new BodyworkStore(this, bodyworkStoreLimit);
        engineStore = new EngineStore(this, engineStoreLimit);
        carStoreController = new CarStoreController(this);
        threadPool = new ArrayList<>();

        for(int i = 0; i < accessorySupplierCount; i++)
            threadPool.add(new AccessorySupplier(this));

        threadPool.add(new EngineSupplier(this));
        threadPool.add(new BodyworkSupplier(this));

        for(int i = 0; i < dealerCount; i++)
            threadPool.add(new Dealer(this, i));

        startThreads();

        initialized = true;
    }

    private void stopThreads()
    {
        for(var thread : threadPool)
            thread.interrupt();

        producerSection.destroy();
        carStoreController.interrupt();
    }

    private void startThreads()
    {
        carStoreController.start();
        producerSection.start();
        for(var thread : threadPool)
            thread.start();
    }

    public synchronized CarStore getCarStore()
    {
        return carStore;
    }

    public synchronized AccessoryStore getAccessoryStore()
    {
        return accessoryStore;
    }

    public synchronized BodyworkStore getBodyworkStore()
    {
        return bodyworkStore;
    }

    public synchronized EngineStore getEngineStore()
    {
        return engineStore;
    }

    public synchronized ProducerSection getProducerSection()
    {
        return producerSection;
    }

    public synchronized EventManager getEventManager()
    {
        return eventManager;
    }

    public synchronized int getSupplierCount()
    {
        return supplierCount;
    }

    public synchronized int getProducerCount()
    {
        return producerCount;
    }

    public synchronized int getDealerCount()
    {
        return dealerCount;
    }

    public synchronized int getSupplierDelay()
    {
        return supplierDelay;
    }

    public synchronized int getProducerDelay()
    {
        return producerDelay;
    }

    public synchronized int getDealerDelay()
    {
        return dealerDelay;
    }

    public synchronized void setSupplierDelay(int supplierDelay)
    {
        if(supplierDelay == -1)
            this.supplierDelay = (int)(randomizeDelay() * 1000);
        else
            this.supplierDelay = supplierDelay;
    }

    public synchronized void setProducerDelay(int producerDelay)
    {
        if(producerDelay == -1)
            this.producerDelay = (int)(randomizeDelay() * 1000);
        else
            this.producerDelay = producerDelay;
    }

    public synchronized void setDealerDelay(int dealerDelay)
    {
        if(dealerDelay == -1)
            this.dealerDelay = (int)(randomizeDelay() * 1000);
        else
            this.dealerDelay = dealerDelay;
    }

    public void enableLogging() throws Exception
    {
        Log.init();
        Log.enableInfoLogging();
    }

    public void disableLogging()
    {
        Log.disableLogging();
        Log.disableInfoLogging();
    }

    public boolean isLogEnabled()
    {
        return Log.isLoggingEnabled();
    }

    /**
     * @return random value from 0 to 2
     */
    public static double randomizeDelay()
    {
        return 2 * ThreadLocalRandom.current().nextDouble();
    }

    //EVENT SECTION

    public void setOnEngineDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.ENGINE_DELIVERED_EVENT, handler);
    }

    public void setOnAccessoryDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.ACCESSORY_DELIVERED_EVENT, handler);
    }

    public void setOnBodyworkDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.BODYWORK_DELIVERED_EVENT, handler);
    }

    public void setOnCareMade(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.CAR_MADE_EVENT, handler);
    }

    public void setOnCarDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.CAR_SUPPLIED_TO_STORE_EVENT, handler);
    }

    public void setOnComponentSendFromStore(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.COMPONENT_SEND_FROM_STORE, handler);
    }

    public void setOnCarSend(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.CAR_SEND_EVENT, handler);
    }

    public void setOnProducerStartJob(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.PRODUCER_STARTED_DO_JOB_EVENT, handler);
    }

    public void setOnProducerDidJob(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.PRODUCER_DID_JOB_EVENT, handler);
    }
}
