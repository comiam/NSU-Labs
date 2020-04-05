package comiam.factoryapp.factory.factory;

import comiam.factoryapp.factory.dealer.Dealer;
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
    private ArrayList<Thread> threadPool;
    private CarStore carStore;
    private AccessoryStore accessoryStore;
    private BodyworkStore bodyworkStore;
    private EngineStore engineStore;
    private ProducerSection producerSection;
    private CarStoreController carStoreController;

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

        Log.disableDebugLogging();
        Log.disableErrorLogging();

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
        carStore = new CarStore(carStoreLimit);
        accessoryStore = new AccessoryStore(accessoryStoreLimit);
        bodyworkStore = new BodyworkStore(bodyworkStoreLimit);
        engineStore = new EngineStore(engineStoreLimit);
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

    public boolean isInitialized()
    {
        return initialized;
    }

    public CarStore getCarStore()
    {
        return carStore;
    }

    public AccessoryStore getAccessoryStore()
    {
        return accessoryStore;
    }

    public BodyworkStore getBodyworkStore()
    {
        return bodyworkStore;
    }

    public EngineStore getEngineStore()
    {
        return engineStore;
    }

    public ProducerSection getProducerSection()
    {
        return producerSection;
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

    /**
     * @return random value from 0 to 2
     */
    private double randomizeDelay()
    {
        return 2 * ThreadLocalRandom.current().nextDouble();
    }
}
