package comiam.factoryapp.factory.factory;

import comiam.factoryapp.factory.controller.CarStoreController;
import comiam.factoryapp.factory.dealer.Dealer;
import comiam.factoryapp.factory.events.EventHandler;
import comiam.factoryapp.factory.events.EventManager;
import comiam.factoryapp.factory.producer.ProducerSection;
import comiam.factoryapp.factory.store.*;
import comiam.factoryapp.factory.supplier.AccessorySupplier;
import comiam.factoryapp.factory.supplier.BodyworkSupplier;
import comiam.factoryapp.factory.supplier.EngineSupplier;
import comiam.factoryapp.io.Log;
import comiam.factoryapp.time.Timer;
import comiam.factoryapp.util.Bundle;

import java.util.ArrayList;
import java.util.Objects;
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

    private final int accessorySupplierCount;
    private final int producerCount;
    private final int dealerCount;
    private final Bundle<Integer> supplierDelay;
    private final Bundle<Integer> producerDelay;
    private final Bundle<Integer> dealerDelay;
    private final Bundle<Boolean> loggingEnabled;
    private final Bundle<Boolean> printName;
    private final Bundle<String> name;
    private final int accessoryStoreLimit;
    private final int engineStoreLimit;
    private final int bodyworkStoreLimit;
    private final int carStoreLimit;
    private int threadPriority;

    /**
     * If any delay equals -1, when this delay sets randomly from 0,2 to 1.
     * @param accessorySupplierCount - count of suppliers for this factory.
     * @param producerCount - count of producers for this factory.
     * @param dealerCount - count of dealers for this factory.
     * @param supplierDelay - delay in milliseconds with which suppliers deliver components. If delay < 10, then it set to 10
     * @param producerDelay - delay in milliseconds with which producers make cars. If delay < 10, then it set to 10
     * @param dealerDelay - delay in milliseconds with which dealers buy cars. If delay < 10, then it set to 10
     * @param accessoryStoreLimit - limit of components for accessory store
     * @param engineStoreLimit - limit of components for engine store
     * @param bodyworkStoreLimit - limit of components for bodywork store
     * @param carStoreLimit - limit of components for car store
     */
    public Factory(int accessorySupplierCount, int producerCount, int dealerCount, int supplierDelay, int producerDelay, int dealerDelay,
                   int accessoryStoreLimit, int engineStoreLimit, int bodyworkStoreLimit, int carStoreLimit, boolean loggingEnabled, String name)
    {
        eventManager = new EventManager();

        if(supplierDelay == -1)
            supplierDelay = (int)(randomizeDelay() * 1000);

        if(producerDelay == -1)
            supplierDelay = (int)(randomizeDelay() * 1000);

        if(dealerDelay == -1)
            supplierDelay = (int)(randomizeDelay() * 1000);

        this.name = new Bundle<>(Objects.requireNonNullElseGet(name, () -> "Factory-" + this.hashCode()));
        this.printName = new Bundle<>(true);

        this.accessorySupplierCount = accessorySupplierCount;
        this.dealerCount = dealerCount;
        this.producerCount = producerCount;
        this.supplierDelay = new Bundle<>(supplierDelay);
        this.producerDelay = new Bundle<>(producerDelay);
        this.dealerDelay = new Bundle<>(dealerDelay);
        this.accessoryStoreLimit = accessoryStoreLimit;
        this.engineStoreLimit = engineStoreLimit;
        this.bodyworkStoreLimit = bodyworkStoreLimit;
        this.carStoreLimit = carStoreLimit;
        this.loggingEnabled = new Bundle<>(loggingEnabled);
    }

    /**
     * Destroy all threads in factory, factory will stop. All data will be deleted.
     */
    public synchronized void destroy(boolean destroyEventListeners)
    {
        if(!initialized)
            return;

        initialized = false;

        carStore = null;
        accessoryStore = null;
        bodyworkStore = null;
        engineStore = null;

        stopThreads();
        threadPool.clear();
        carStoreController = null;
        producerSection = null;
        threadPool = null;

        if(destroyEventListeners)
            eventManager = null;
        System.gc();
        Timer.stop();
    }

    /**
     * Init all factory processes: Dealers, Producers and suppliers.
     * @param threadPriority - thread priority of all threads in factory process
     * @throws Exception - if enable logging is failed
     */
    public synchronized void init(int threadPriority) throws Exception
    {
        if(initialized)
            return;

        carStore = new CarStore(eventManager, carStoreLimit);
        accessoryStore = new AccessoryStore(eventManager, accessoryStoreLimit);
        bodyworkStore = new BodyworkStore(eventManager, bodyworkStoreLimit);
        engineStore = new EngineStore(eventManager, engineStoreLimit);
        producerSection = new ProducerSection(this, threadPriority);
        carStoreController = new CarStoreController(producerSection.getPool(), carStore, threadPriority);
        threadPool = new ArrayList<>();

        for(int i = 0; i < accessorySupplierCount; i++)
            threadPool.add(new AccessorySupplier(this, threadPriority));

        threadPool.add(new EngineSupplier(this, threadPriority));
        threadPool.add(new BodyworkSupplier(this, threadPriority));

        for(int i = 0; i < dealerCount; i++)
            threadPool.add(new Dealer(this, i, threadPriority));

        this.threadPriority = threadPriority;

        initialized = true;
        if(loggingEnabled.getVal())
            enableLogging();
        Timer.start();
        startThreads();
    }

    public synchronized void restart(int threadPriority) throws Exception
    {
        if(!initialized)
            return;

        destroy(false);
        if(threadPriority != -1)
            this.threadPriority = threadPriority;

        init(this.threadPriority);
    }

    private synchronized void stopThreads()
    {
        producerSection.destroy();
        carStoreController.interrupt();

        for(var thread : threadPool)
            thread.interrupt();
    }

    private void startThreads()
    {
        carStoreController.start();
        producerSection.start();
        for(var thread : threadPool)
            thread.start();
    }

    public synchronized Bundle<Boolean> canPrintName()
    {
        return printName;
    }

    public synchronized void setPrintFactoryNameToLog(boolean set)
    {
        printName.setVal(set);
    }

    public synchronized void setFactoryName(String name)
    {
        this.name.setVal(name);
    }

    public synchronized Bundle<String> getFactoryName()
    {
        return name;
    }

    public synchronized boolean isInitialized()
    {
        return initialized;
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

    public synchronized int getAccessorySupplierCount()
    {
        return accessorySupplierCount;
    }

    public synchronized int getProducerCount()
    {
        return producerCount;
    }

    public synchronized int getDealerCount()
    {
        return dealerCount;
    }

    public synchronized Bundle<Integer> getSupplierDelay()
    {
        return supplierDelay;
    }

    public synchronized Bundle<Integer> getProducerDelay()
    {
        return producerDelay;
    }

    public synchronized Bundle<Integer> getDealerDelay()
    {
        return dealerDelay;
    }

    public synchronized void setSupplierDelay(int supplierDelay)
    {
        if(supplierDelay == -1)
            this.supplierDelay.setVal((int)(randomizeDelay() * 1000));
        else
            this.supplierDelay.setVal(supplierDelay);
    }

    public synchronized void setProducerDelay(int producerDelay)
    {
        if(producerDelay == -1)
            this.producerDelay.setVal((int)(randomizeDelay() * 1000));
        else
            this.producerDelay.setVal(producerDelay);
    }

    public synchronized void setDealerDelay(int dealerDelay)
    {
        if(dealerDelay == -1)
            this.dealerDelay.setVal((int)(randomizeDelay() * 1000));
        else
            this.dealerDelay.setVal(dealerDelay);
    }

    public synchronized Bundle<Boolean> isLoggingEnabled()
    {
        if(loggingEnabled.getVal() && !Log.isLoggingEnabled())
            try
            {
                enableLogging();
            }catch(Throwable ignored) {
                loggingEnabled.setVal(false);
            }

        return loggingEnabled;
    }

    public synchronized void disableLogging()
    {
        loggingEnabled.setVal(false);
    }

    public synchronized void enableLogging() throws Exception
    {
        loggingEnabled.setVal(true);
        Log.init();
        Log.enableInfoLogging();
    }

    /**
     * @return random value from 0 to 2
     */
    public static double randomizeDelay()
    {
        return 2 * ThreadLocalRandom.current().nextDouble();
    }

    //EVENT SECTION

    public synchronized void setOnEngineDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.ENGINE_DELIVERED_EVENT, handler);
    }

    public synchronized void setOnAccessoryDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.ACCESSORY_DELIVERED_EVENT, handler);
    }

    public synchronized void setOnBodyworkDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.BODYWORK_DELIVERED_EVENT, handler);
    }

    public synchronized void setOnCarMade(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.CAR_MADE_EVENT, handler);
    }

    public synchronized void setOnCarDelivered(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.CAR_SUPPLIED_TO_STORE_EVENT, handler);
    }

    public synchronized void setOnComponentSendFromStore(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.COMPONENT_SEND_FROM_STORE, handler);
    }

    public synchronized void setOnCarSend(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.CAR_SEND_EVENT, handler);
    }

    public synchronized void setOnProducerStartJob(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.PRODUCER_STARTED_DO_JOB_EVENT, handler);
    }

    public synchronized void setOnProducerDidJob(EventHandler handler)
    {
        if(handler == null)
            return;
        eventManager.setEventHandler(EventManager.PRODUCER_DID_JOB_EVENT, handler);
    }
}