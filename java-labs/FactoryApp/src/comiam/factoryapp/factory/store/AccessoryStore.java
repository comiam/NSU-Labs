package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Accessory;
import comiam.factoryapp.factory.factory.Factory;

public class AccessoryStore extends Store<Accessory>
{
    public AccessoryStore(Factory factory, int limit)
    {
        super(factory, limit);
    }
}
