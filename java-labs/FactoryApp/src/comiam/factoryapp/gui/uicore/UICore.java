package comiam.factoryapp.gui.uicore;

import comiam.factoryapp.factory.components.Accessory;
import comiam.factoryapp.factory.components.Bodywork;
import comiam.factoryapp.factory.components.Car;
import comiam.factoryapp.factory.components.Engine;
import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.dialogs.Dialogs;
import comiam.factoryapp.gui.fxml.MainWindowController;
import comiam.factoryapp.time.Timer;
import javafx.application.Platform;

public class UICore
{
    private static Factory factory = null;
    private static MainWindowController controller = null;

    public static Factory getFactory()
    {
        return factory;
    }

    public static void initCore(MainWindowController controller)
    {
        UICore.controller = controller;
    }

    public static void enableFactoryProcess(int accessorySupplierCount, int producerCount, int dealerCount, int supplierDelay, int producerDelay, int dealerDelay,
                                            int accessoryStoreLimit, int engineStoreLimit, int bodyworkStoreLimit, int carStoreLimit, boolean loggingEnabled)
    {
        factory = new Factory();
        setEventHandlers();
        if(loggingEnabled)
        {
            try
            {
                factory.enableLogging();
            }catch(Throwable e)
            {
                Dialogs.showExceptionDialog(e, "Can't initialize logging!");
            }
        }

        factory.init(accessorySupplierCount, producerCount, dealerCount, supplierDelay, producerDelay, dealerDelay,
                accessoryStoreLimit, engineStoreLimit, bodyworkStoreLimit, carStoreLimit);

        Timer.start(() -> Platform.runLater(() -> controller.setWorkingTime(Timer.getSeconds() / 3600 + ":" + Timer.getSeconds() / 60 + ":" + Timer.getMilliSeconds())));


        controller.setDealerCount(factory.getDealerCount());
        controller.setProducerCount(factory.getProducerCount());
        controller.setSupplierCount(factory.getSupplierCount());
    }

    private static void setEventHandlers()
    {
        factory.setOnAccessoryDelivered((o) ->
        {
            UIDataBundle.incAccessoryDelivered();
            Platform.runLater(() -> {
                controller.setAccessoryDelivered(UIDataBundle.getAccessoryDelivered());
                controller.setAccessoryStoreCount(factory.getAccessoryStore().getCurrentCount());
            });
        });

        factory.setOnBodyworkDelivered((o) ->
        {
            UIDataBundle.incBodyworkDelivered();
            Platform.runLater(() -> {
                controller.setBodyworkDelivered(UIDataBundle.getBodyworkDelivered());
                controller.setBodyworkStoreCount(factory.getBodyworkStore().getCurrentCount());
            });
        });

        factory.setOnEngineDelivered((o) ->
        {
            UIDataBundle.incEngineDelivered();
            Platform.runLater(() -> {
                controller.setEngineDelivered(UIDataBundle.getEngineDelivered());
                controller.setEngineStoreCount(factory.getEngineStore().getCurrentCount());
            });
        });

        factory.setOnCarDelivered((o) -> Platform.runLater(() -> controller.setCarStoreCount(factory.getCarStore().getCurrentCount())));

        factory.setOnCareMade((o) ->
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
                controller.setCarsSend(UIDataBundle.getCarSendCount());
                controller.printLog("Dealer " + array[0] + ": Auto: " + ((Car) array[1]).getUniqueID() + "; (Body: " +
                                    ((Car) array[1]).getBodywork().getUniqueID() + "; Engine: " +
                                    ((Car) array[1]).getEngine().getUniqueID() + "; Accessory: " +
                                    ((Car) array[1]).getAccessory().getUniqueID() + ")");
            });
        });

        factory.setOnProducerStartJob((o) -> {
            UIDataBundle.incFactoryWorkingProducers();
            Platform.runLater(() -> controller.setFactoryLoad((UIDataBundle.getFactoryWorkingProducers() / factory.getProducerCount()) * 100 + "%"));
        });

        factory.setOnProducerDidJob((o) -> {
            UIDataBundle.decFactoryWorkingProducers();
            Platform.runLater(() -> controller.setFactoryLoad((UIDataBundle.getFactoryWorkingProducers() / factory.getProducerCount()) * 100 + "%"));
        });

        factory.setOnComponentSendFromStore((o) ->
                Platform.runLater(() -> {
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
