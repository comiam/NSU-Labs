package comiam.factoryapp.factory.supplier;

import comiam.factoryapp.factory.components.Engine;
import comiam.factoryapp.factory.factory.Factory;

public class EngineSupplier extends Thread
{
    public EngineSupplier(Factory factory, int priority)
    {
        super(new Supplier<>(factory, factory.getEngineStore(), Engine.class), "Supplier");
        setPriority(priority);
    }
}
