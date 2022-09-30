package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Accessory;
import comiam.factoryapp.factory.events.EventManager;

public class AccessoryStore extends Store<Accessory>
{
    public AccessoryStore(EventManager manager, int limit)
    {
        super(manager, limit);
    }
}
