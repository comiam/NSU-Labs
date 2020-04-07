package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.factory.Factory;

public class CarStore extends Store<Car>
{
    public CarStore(Factory factory, int limit)
    {
        super(factory, limit);
    }
}