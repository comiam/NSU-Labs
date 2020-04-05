package comiam.factoryapp.factory.supplier;

import comiam.factoryapp.factory.components.Accessory;
import comiam.factoryapp.factory.factory.Factory;

public class AccessorySupplier extends Thread
{
    public AccessorySupplier(Factory factory)
    {
        super(new Supplier<>(factory, factory.getAccessoryStore(), Accessory.class));
    }
}
