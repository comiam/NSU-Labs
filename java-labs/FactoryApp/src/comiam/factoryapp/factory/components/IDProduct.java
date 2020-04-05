package comiam.factoryapp.factory.components;

public class IDProduct
{
    private static long ID;
    protected long uniqueID;

    public IDProduct(long uniqueID)
    {
        this.uniqueID = uniqueID;
    }

    public static synchronized long getID()
    {
        return ID++;
    }

    public synchronized long getUniqueID()
    {
        return uniqueID;
    }
}
