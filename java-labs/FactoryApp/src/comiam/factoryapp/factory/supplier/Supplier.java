package comiam.factoryapp.factory.supplier;

import comiam.factoryapp.factory.components.IDProduct;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.factory.store.Store;
import comiam.factoryapp.util.Bundle;

import static comiam.factoryapp.util.ThreadChecker.assertThreadInterrupted;

public class Supplier<T extends IDProduct> implements Runnable
{
    private final Store<T> store;
    private final Class<T> typeClass;
    private final Bundle<Integer> delay;

    public Supplier(Factory currentFactory, Store<T> store, Class<T> typeClass)
    {
        this.store = store;
        this.typeClass = typeClass;
        this.delay = currentFactory.getSupplierDelay();
    }

    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                if(delay.getVal() != 0)
                    Thread.sleep(delay.getVal());

                store.putComponent(typeClass.getConstructor(Long.TYPE).newInstance(IDProduct.getID()));

                assertThreadInterrupted();
            }catch(Throwable ignored) {
                break;
            }
        }
    }
}
