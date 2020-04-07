package comiam.factoryapp.factory.supplier;

import comiam.factoryapp.factory.components.IDProduct;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.factory.store.Store;

public class Supplier<T extends IDProduct> implements Runnable
{
    private final Factory  currentFactory;
    private final Store<T> store;
    private final Class<T> typeClass;

    public Supplier(Factory currentFactory, Store<T> store, Class<T> typeClass)
    {
        this.currentFactory = currentFactory;
        this.store = store;
        this.typeClass = typeClass;
    }

    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                Thread.sleep(currentFactory.getSupplierDelay());
            } catch(InterruptedException ignored) {}

            try
            {
                store.putComponent(typeClass.getConstructor(Long.TYPE).newInstance(IDProduct.getID()));
            }catch(Throwable ignored) {}
        }
    }
}
