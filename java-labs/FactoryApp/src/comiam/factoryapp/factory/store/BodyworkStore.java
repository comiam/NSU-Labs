package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Bodywork;
import comiam.factoryapp.factory.events.EventManager;

public class BodyworkStore extends Store<Bodywork>
{
    public BodyworkStore(EventManager manager, int limit)
    {
        super(manager, limit);
    }
}