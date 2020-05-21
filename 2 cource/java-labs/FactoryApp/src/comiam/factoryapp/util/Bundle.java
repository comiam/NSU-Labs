package comiam.factoryapp.util;

public class Bundle<T>
{
    private T val;

    public Bundle(T val)
    {
        this.val = val;
    }

    public synchronized void setVal(T val)
    {
        this.val = val;
    }

    public synchronized T getVal()
    {
        return val;
    }
}
