package comiam.factoryapp.factory.supplier;

import comiam.factoryapp.factory.components.Bodywork;
import comiam.factoryapp.factory.factory.Factory;

public class BodyworkSupplier extends Thread
{
    public BodyworkSupplier(Factory factory, int priority)
    {
        super(new Supplier<>(factory, factory.getBodyworkStore(), Bodywork.class), "Supplier");
        setPriority(priority);
    }
}
