package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.events.EventManager;

public class CarStore extends Store<Car>
{
    public CarStore(EventManager manager, int limit)
    {
        super(manager, limit);
    }
}