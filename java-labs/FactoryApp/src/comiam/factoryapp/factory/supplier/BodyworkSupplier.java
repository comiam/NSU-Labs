package comiam.factoryapp.factory.supplier;

import comiam.factoryapp.factory.components.Bodywork;
import comiam.factoryapp.factory.factory.Factory;

public class BodyworkSupplier extends Thread
{
    public BodyworkSupplier(Factory factory)
    {
        super(new Supplier<>(factory, factory.getBodyworkStore(), Bodywork.class));
    }
}
