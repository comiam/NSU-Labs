package comiam.factoryapp.factory.dealer;

import comiam.factoryapp.factory.factory.Factory;

public class Dealer extends Thread
{
    public Dealer(Factory factory, int ID, int priority)
    {
        super(new DealerRunnable(factory, ID), "Dealer");
        setPriority(priority);
    }
}
