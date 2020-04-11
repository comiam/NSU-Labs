package comiam.factoryapp.factory.dealer;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.events.EventManager;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.io.Log;
import comiam.factoryapp.time.Timer;

import static comiam.factoryapp.util.ThreadChecker.assertThreadInterrupted;

public class DealerRunnable implements Runnable
{
    private final Factory factory;
    private final int ID;

    public DealerRunnable(Factory factory, int ID)
    {
        this.factory = factory;
        this.ID = ID;
    }

    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                Thread.sleep(factory.getDealerDelay());
                Car car = factory.getCarStore().getComponent();

                assertThreadInterrupted();

                factory.getEventManager().fireEvent(EventManager.CAR_SEND_EVENT, new Object[]{ID, car});

                if(factory.isLoggingEnabled())
                    Log.info(Timer.getTime(Timer.ALL_PARAMETERS) + " - Dealer " + ID + ": Auto: " + car.getUniqueID() + "; (Body: " +
                                    car.getBodywork().getUniqueID() + "; Engine: " + car.getEngine().getUniqueID() + "; Accessory: " + car.getAccessory().getUniqueID() + ")");
            } catch(Throwable ignored) {
                break;
            }
        }
    }
}
