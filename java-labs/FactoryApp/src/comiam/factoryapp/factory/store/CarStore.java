package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Car;

public class CarStore extends Store<Car>
{
    public CarStore(int limit)
    {
        super(limit);
    }
}