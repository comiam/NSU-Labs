package comiam.factoryapp.factory.dealer;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.events.EventManager;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.factory.store.CarStore;
import comiam.factoryapp.io.Log;
import comiam.factoryapp.time.Timer;
import comiam.factoryapp.util.Bundle;

import static comiam.factoryapp.util.ThreadChecker.assertThreadInterrupted;

public class DealerRunnable implements Runnable
{
    private final int ID;
    private final Bundle<Integer> delay;
    private final Bundle<Boolean> isLoggingEnabled;
    private final Bundle<Boolean> canPrintName;
    private final Bundle<String> factoryName;
    private final CarStore store;
    private final EventManager eventManager;

    public DealerRunnable(Factory factory, int ID)
    {
        this.ID = ID;
        this.delay = factory.getDealerDelay();
        this.store = factory.getCarStore();
        this.eventManager = factory.getEventManager();
        this.isLoggingEnabled = factory.isLoggingEnabled();
        this.canPrintName = factory.canPrintName();
        this.factoryName = factory.getFactoryName();
    }

    @Override
    public void run()
    {
        String time;
        while(!Thread.currentThread().isInterrupted())
        {
            try
            {
                if(delay.getVal() != 0)
                    Thread.sleep(delay.getVal());
                Car car = store.getComponent();

                assertThreadInterrupted();

                time = Timer.getTime(Timer.ALL_PARAMETERS);

                eventManager.fireEvent(EventManager.CAR_SEND_EVENT, new Object[]{time, ID, car});

                if(isLoggingEnabled.getVal())
                    Log.info(time + (canPrintName.getVal() ? " [" + factoryName.getVal() + "]" : "") + " - Dealer " + ID + ": Auto: " + car.getUniqueID() + "; (Body: " +
                             car.getBodywork().getUniqueID() + "; Engine: " + car.getEngine().getUniqueID() + "; Accessory: " + car.getAccessory().getUniqueID() + ")");
            } catch(Throwable ignored) {
                break;
            }
        }
    }
}
