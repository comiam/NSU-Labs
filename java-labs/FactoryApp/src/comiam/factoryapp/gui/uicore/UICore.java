package comiam.factoryapp.gui.uicore;

import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.components.IDProduct;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.dialogs.Dialogs;
import comiam.factoryapp.gui.fxml.MainWindowController;
import comiam.factoryapp.time.Timer;
import javafx.application.Platform;

public class UICore
{
    private static Factory factory = null;
    private static MainWindowController controller = null;
    private static UIThread uiThread;

    public static synchronized Factory getFactory()
    {
        return factory;
    }

    public static synchronized void initCore(MainWindowController controller)
    {
        UICore.controller = controller;
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    }

    public static synchronized void enableFactoryProcess(Factory factory)
    {
        if(UICore.factory != null && UICore.factory.isInitialized())
            UICore.factory.destroy(true);

        UICore.factory = factory;
        setEventHandlers();

        UIDataBundle.resetAll();
        IDProduct.resetID();
        UIDataBundle.initDealerLogDataBundle(factory.getDealerCount());
        uiThread = new UIThread(factory, controller);

        uiThread.start();
        try
        {
            UICore.factory.init(Thread.MIN_PRIORITY);
        } catch(Exception e)
        {
            Dialogs.showExceptionDialog(controller.getRootStage(), e, "Can't initialize logging!");
        }
        setTimerEventHandler();

        controller.clearFields();
        controller.setDealerCount(UICore.factory.getDealerCount());
        controller.setProducerCount(UICore.factory.getProducerCount());
        controller.setSupplierCount(UICore.factory.getAccessorySupplierCount());
        controller.setStatus(MainWindowController.FactoryStatus.RUNNING);
        controller.setPDSliderVal(UICore.factory.getProducerDelay().getVal());
        controller.setSDSliderVal(UICore.factory.getSupplierDelay().getVal());
        controller.setDDSliderVal(UICore.factory.getDealerDelay().getVal());
        controller.setCBLogging(UICore.factory.isLoggingEnabled().getVal());

        controller.resetLog();
        controller.enableAll();
    }

    public static synchronized void enableFactoryProcess(int accessorySupplierCount, int producerCount, int dealerCount, int supplierDelay, int producerDelay, int dealerDelay,
                                                         int accessoryStoreLimit, int engineStoreLimit, int bodyworkStoreLimit, int carStoreLimit, boolean loggingEnabled)
    {
        enableFactoryProcess(new Factory(accessorySupplierCount, producerCount, dealerCount, supplierDelay, producerDelay, dealerDelay,
                accessoryStoreLimit, engineStoreLimit, bodyworkStoreLimit, carStoreLimit, loggingEnabled, "AppFactory-" + ProcessHandle.current().pid()));
    }

    public static synchronized void restartFactory()
    {
        if(factory == null || !factory.isInitialized())
            return;

        UIDataBundle.resetAll();
        IDProduct.resetID();
        uiThread.waitOnRestart();

        try
        {
            factory.restart(-1);
        } catch(Exception e)
        {
            Dialogs.showExceptionDialog(controller.getRootStage(), e, "Can't initialize logging!");
        }

        uiThread.continueWork();
        setTimerEventHandler();

        controller.clearFields();
        controller.setCBLogging(false);
        controller.setSDSliderVal(factory.getSupplierDelay().getVal());
        controller.setPDSliderVal(factory.getProducerDelay().getVal());
        controller.setDDSliderVal(factory.getDealerDelay().getVal());
        controller.enableAll();
        controller.resetLog();
    }

    private static void setTimerEventHandler()
    {
        Timer.subscribeEvent(() ->
        {
            if(!Timer.isRunning())
                return;
            String time = Timer.getTime(Timer.HOURS | Timer.MINUTES | Timer.SECONDS);
            Platform.runLater(() ->
                    controller.setWorkingTime(time)
            );
        }, 1000);
    }

    public static synchronized boolean factoryIsDisabled()
    {
        return factory == null;
    }

    public static synchronized void disableFactory()
    {
        if(factory != null)
        {
            factory.destroy(true);
            factory = null;
        }
        if(uiThread != null)
            uiThread.interrupt();
    }

    private static synchronized void setEventHandlers()
    {
        factory.setOnAccessoryDelivered((o) -> UIDataBundle.incAccessoryDelivered());
        factory.setOnBodyworkDelivered((o) -> UIDataBundle.incBodyworkDelivered());
        factory.setOnEngineDelivered((o) -> UIDataBundle.incEngineDelivered());
        factory.setOnCarMade((o) -> UIDataBundle.incCarMadeCount());
        factory.setOnProducerStartJob((o) -> UIDataBundle.incFactoryWorkingProducers());
        factory.setOnProducerDidJob((o) -> UIDataBundle.decFactoryWorkingProducers());

        factory.setOnCarSend((o) ->
        {
            Object[] array = (Object[]) o;
            if(array[0] == null || array[1] == null || array[2] == null)
                return;
            if(((Car) array[2]).getBodywork() == null || ((Car) array[2]).getEngine() == null || ((Car) array[2]).getAccessory() == null)
                return;

            UIDataBundle.incCarSendCount();
            UIDataBundle.addLogData((Integer) array[1], array[0], array[2]);
        });
    }
}
