package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Engine;
import comiam.factoryapp.factory.events.EventManager;

public class EngineStore extends Store<Engine>
{
    public EngineStore(EventManager manager, int limit)
    {
        super(manager, limit);
    }
}