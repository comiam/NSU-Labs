package comiam.factoryapp.factory.dealer;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.log.Log;

public class Dealer extends Thread
{
    public Dealer(Factory factory, int ID)
    {
        super(() ->
                {
                    while(!Thread.currentThread().isInterrupted())
                    {
                        try
                        {
                            Thread.sleep(factory.getDealerDelay());

                            Car car = factory.getCarStore().getComponent();
                            Log.info("Dealer " + ID + ": Auto: " + car.getUniqueID() + "; (Body: " +
                                     car.getBodywork().getUniqueID() + "; Engine: " + car.getEngine().getUniqueID() + "; Accessory: " + car.getAccessory().getUniqueID() + ")");
                        } catch(InterruptedException ignored) {}
                    }
                }
        );
    }
}
