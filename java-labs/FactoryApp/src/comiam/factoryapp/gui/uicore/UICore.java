package comiam.factoryapp.gui.uicore;

import comiam.factoryapp.factory.components.*;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.dialogs.Dialogs;
import comiam.factoryapp.gui.fxml.MainWindowController;
import comiam.factoryapp.time.Timer;
import javafx.application.Platform;

public class UICore
{
    private static Factory factory = null;
    private static MainWindowController controller = null;

    public static synchronized Factory getFactory()
    {
        return factory;
    }

    public static synchronized void initCore(MainWindowController controller)
    {
        UICore.controller = controller;
    }

    public static synchronized void enableFactoryProcess(Factory factory)
    {
        if(UICore.factory != null && UICore.factory.isInitialized())
            UICore.factory.destroy(true);

        UICore.factory = factory;
        setEventHandlers();
        UIDataBundle.resetAll();
        IDProduct.resetID();

        try
        {
            UICore.factory.init();
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
        controller.setPDSliderVal(UICore.factory.getProducerDelay());
        controller.setSDSliderVal(UICore.factory.getSupplierDelay());
        controller.setDDSliderVal(UICore.factory.getDealerDelay());
        controller.setCBLogging(UICore.factory.isLoggingEnabled());

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
        try
        {
            factory.restart();
        } catch(Exception e)
        {
            Dialogs.showExceptionDialog(controller.getRootStage(), e, "Can't initialize logging!");
        }
        setTimerEventHandler();

        controller.clearFields();
        controller.setCBLogging(false);
        controller.setSDSliderVal(factory.getSupplierDelay());
        controller.setPDSliderVal(factory.getProducerDelay());
        controller.setDDSliderVal(factory.getDealerDelay());
        controller.enableAll();
        controller.resetLog();
    }

    private static void setTimerEventHandler()
    {
        Timer.subscribeEvent(() -> {
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
    }

    private static synchronized void setEventHandlers()
    {
        factory.setOnAccessoryDelivered((o) ->
        {
            UIDataBundle.incAccessoryDelivered();
            Platform.runLater(() -> {
                if(factory == null)
                    return;
                controller.setAccessoryDelivered(UIDataBundle.getAccessoryDelivered());
                controller.setAccessoryStoreCount(factory.getAccessoryStore().getCurrentCount());
            });
        });

        factory.setOnBodyworkDelivered((o) ->
        {
            UIDataBundle.incBodyworkDelivered();
            Platform.runLater(() -> {
                if(factory == null)
                    return;
                controller.setBodyworkDelivered(UIDataBundle.getBodyworkDelivered());
                controller.setBodyworkStoreCount(factory.getBodyworkStore().getCurrentCount());
            });
        });

        factory.setOnEngineDelivered((o) ->
        {
            UIDataBundle.incEngineDelivered();
            Platform.runLater(() -> {
                if(factory == null)
                    return;
                controller.setEngineDelivered(UIDataBundle.getEngineDelivered());
                controller.setEngineStoreCount(factory.getEngineStore().getCurrentCount());
            });
        });

        factory.setOnCarDelivered((o) -> Platform.runLater(() -> {
            if(factory == null)
                return;
            controller.setCarStoreCount(factory.getCarStore().getCurrentCount());
        }));

        factory.setOnCarMade((o) ->
        {
            UIDataBundle.incCarMadeCount();
            Platform.runLater(() -> controller.setCarsMade(UIDataBundle.getCarMadeCount()));
        });

        factory.setOnCarSend((o) ->
        {
            Object[] array = (Object[]) o;
            UIDataBundle.incCarSendCount();
            Platform.runLater(() ->
            {
                if(array[0] == null || array[1] == null)
                    return;
                if(((Car) array[1]).getBodywork() == null || ((Car) array[1]).getEngine() == null || ((Car) array[1]).getAccessory() == null)
                    return;
                controller.setCarsSend(UIDataBundle.getCarSendCount());
                if(Timer.isRunning())
                    controller.printLog(Timer.getTime(Timer.ALL_PARAMETERS) + " - Dealer " + array[0] + ": Auto: " + ((Car) array[1]).getUniqueID() + "; (Body: " +
                                        ((Car) array[1]).getBodywork().getUniqueID() + "; Engine: " +
                                        ((Car) array[1]).getEngine().getUniqueID() + "; Accessory: " +
                                        ((Car) array[1]).getAccessory().getUniqueID() + ")");
            });
        });

        factory.setOnProducerStartJob((o) -> {
            UIDataBundle.incFactoryWorkingProducers();
            Platform.runLater(() -> {
                if(factory == null)
                    return;
                controller.setFactoryLoad(Math.round((1.0 * UIDataBundle.getFactoryWorkingProducers()) / factory.getProducerCount() * 100 * 100) / 100 + "%");
            });
        });

        factory.setOnProducerDidJob((o) -> {
            UIDataBundle.decFactoryWorkingProducers();
            Platform.runLater(() -> {
                if(factory == null)
                    return;
                controller.setFactoryLoad(Math.round((1.0 * UIDataBundle.getFactoryWorkingProducers()) / factory.getProducerCount() * 100 * 100) / 100 + "%");
            });
        });

        factory.setOnComponentSendFromStore((o) ->
                Platform.runLater(() -> {
                    if(factory == null)
                        return;
                    if(o instanceof Accessory)
                        controller.setAccessoryStoreCount(factory.getAccessoryStore().getCurrentCount());
                    else if(o instanceof Engine)
                        controller.setEngineStoreCount(factory.getEngineStore().getCurrentCount());
                    else if(o instanceof Bodywork)
                        controller.setBodyworkStoreCount(factory.getBodyworkStore().getCurrentCount());
                    else if(o instanceof Car)
                        controller.setCarStoreCount(factory.getCarStore().getCurrentCount());
        }));
    }
}
