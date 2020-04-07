package comiam.factoryapp.factory.store;

import comiam.factoryapp.factory.components.Engine;
import comiam.factoryapp.factory.factory.Factory;

public class EngineStore extends Store<Engine>
{
    public EngineStore(Factory factory, int limit)
    {
        super(factory, limit);
    }
}