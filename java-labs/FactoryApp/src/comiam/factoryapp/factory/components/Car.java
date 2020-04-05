package comiam.factoryapp.factory.components;

public class Car extends IDProduct
{
    private final Engine engine;
    private final Accessory accessory;
    private final Bodywork bodywork;

    public Car(long ID, Engine engine, Bodywork bodywork, Accessory accessory)
    {
        super(ID);
        this.engine = engine;
        this.bodywork = bodywork;
        this.accessory = accessory;
    }

    public Engine getEngine()
    {
        return engine;
    }

    public Accessory getAccessory()
    {
        return accessory;
    }

    public Bodywork getBodywork()
    {
        return bodywork;
    }
}
