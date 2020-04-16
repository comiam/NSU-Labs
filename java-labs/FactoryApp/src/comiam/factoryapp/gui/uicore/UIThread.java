package comiam.factoryapp.gui.uicore;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.fxml.MainWindowController;
import comiam.factoryapp.time.Timer;
import comiam.factoryapp.util.Bundle;
import javafx.application.Platform;

import static comiam.factoryapp.util.ThreadChecker.assertThreadInterrupted;

public class UIThread
{
    private static final Bundle<Boolean> DO_NOT_CLOSE = new Bundle<>(false);
    public static final long UPDATE_DELAY = 10;
    private final Thread uiThread;

    public UIThread(Factory factory, MainWindowController controller)
    {
        Runnable uiTask = () ->
        {
            while(!Thread.currentThread().isInterrupted())
            {
                if(DO_NOT_CLOSE.getVal())
                    synchronized(DO_NOT_CLOSE)
                    {
                        try
                        {
                            DO_NOT_CLOSE.wait();
                        } catch(InterruptedException e)
                        {
                            break;
                        }
                    }

                if(!factory.isInitialized())
                    break;

                try
                {
                    for(int i = 0; i < factory.getDealerCount(); i++)
                    {
                        Object[] arr = (Object[])UIDataBundle.getDealerLodData(i);
                        if(arr == null)
                            continue;

                        String time = (String) arr[0];
                        Car car = (Car) arr[1];

                        assertThreadInterrupted();

                        String str = time + " - Dealer " + i + ": Auto: " + car.getUniqueID() + "; (Body: " +
                                     car.getBodywork().getUniqueID() + "; Engine: " +
                                     car.getEngine().getUniqueID() + "; Accessory: " +
                                     car.getAccessory().getUniqueID() + ")";

                        Platform.runLater(() ->
                        {
                            if(Timer.isRunning())
                                controller.printLog(str);
                        });
                    }


                    final long tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8, tmp9;
                    final String tmp10;

                    tmp0 = UIDataBundle.getAccessoryDelivered();

                    assertThreadInterrupted();

                    tmp1 = factory.getAccessoryStore().getCurrentCount();

                    assertThreadInterrupted();

                    tmp2 = UIDataBundle.getBodyworkDelivered();

                    assertThreadInterrupted();

                    tmp3 = UIDataBundle.getEngineDelivered();

                    assertThreadInterrupted();

                    tmp4 = UIDataBundle.getCarMadeCount();

                    assertThreadInterrupted();

                    tmp5 = UIDataBundle.getCarSendCount();

                    assertThreadInterrupted();

                    tmp6 = factory.getAccessoryStore().getCurrentCount();

                    assertThreadInterrupted();

                    tmp7 = factory.getEngineStore().getCurrentCount();

                    assertThreadInterrupted();

                    tmp8 = factory.getBodyworkStore().getCurrentCount();

                    assertThreadInterrupted();

                    tmp9 = factory.getCarStore().getCurrentCount();

                    assertThreadInterrupted();

                    tmp10 = Math.round((1.0 * UIDataBundle.getFactoryWorkingProducers()) / factory.getProducerCount() * 100 * 100) / 100 + "%";

                    Platform.runLater(() ->
                    {
                        controller.setAccessoryDelivered(tmp0);
                        controller.setAccessoryStoreCount(tmp1);
                        controller.setBodyworkDelivered(tmp2);
                        controller.setEngineDelivered(tmp3);
                        controller.setCarsMade(tmp4);
                        controller.setCarsSend(tmp5);
                        controller.setAccessoryStoreCount(tmp6);
                        controller.setEngineStoreCount(tmp7);
                        controller.setBodyworkStoreCount(tmp8);
                        controller.setCarStoreCount(tmp9);
                        controller.setFactoryLoad(tmp10);
                    });

                    Thread.sleep(UPDATE_DELAY);
                }catch(Throwable ignored)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        uiThread = new Thread(uiTask, "UIThread");
    }

    public void waitOnRestart()
    {
        DO_NOT_CLOSE.setVal(true);
    }

    public void continueWork()
    {
        DO_NOT_CLOSE.setVal(false);
        synchronized(DO_NOT_CLOSE)
        {
            DO_NOT_CLOSE.notify();
        }
    }

    public void start()
    {
        uiThread.start();
    }

    public void interrupt()
    {
        uiThread.interrupt();
    }
}
