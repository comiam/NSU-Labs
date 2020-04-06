package comiam.factoryapp.gui.fxml;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.uithread.ThreadUpdater;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;

public class MainWindowController
{
    @FXML
    private Slider sdSlider;

    @FXML
    private Slider pdSlider;

    @FXML
    private Slider ddSlider;

    @FXML
    private Button sdRandom;

    @FXML
    private Button pdRandom;

    @FXML
    private Button ddRandom;

    @FXML
    private Label workingTimeLabel;

    @FXML
    private Label factoryLoadLabel;

    @FXML
    private Label carsMadeLabel;

    @FXML
    private Label carsSuppliedLabel;

    @FXML
    private Label engineDeliveredLabel;

    @FXML
    private Label accessoryDeliveredLabel;

    @FXML
    private Label bodyworkDeliveredLabel;

    @FXML
    private Label bStoreCapacityLabel;

    @FXML
    private Label aStoreCapacityLabel;

    @FXML
    private Label eStoreCapacityLabel;

    @FXML
    private MenuItem saveDataMenu;

    @FXML
    private MenuItem loadConfigMenu;

    @FXML
    private MenuItem quitMenu;

    private Factory factory = null;
    private Thread  uiThread = null;

    public void setFactory(Factory factory)
    {
        this.factory = factory;
    }

    public void runUIThread()
    {
        //FIXME чото тут должно быть с заводом
        uiThread = new Thread(new ThreadUpdater(this, factory));
        uiThread.setName("FactoryUIThread");
        uiThread.start();
    }

    public void closeUIThread()
    {
        if(uiThread == null)
            return;
        uiThread.interrupt();
        uiThread = null;
        factory.destroy();
    }

    public void initialize()
    {
        sdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(factory != null)
            {
                synchronized(factory)
                {
                    factory.setSupplierDelay(newValue.intValue());
                }
            }
        });

        pdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(factory != null)
            {
                synchronized(factory)
                {
                    factory.setProducerDelay(newValue.intValue());
                }
            }
        });

        ddSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(factory != null)
            {
                synchronized(factory)
                {
                    factory.setDealerDelay(newValue.intValue());
                }
            }
        });
    }

    @FXML
    private void randomizeDealerDelay(ActionEvent even)
    {

    }

    @FXML
    private void randomizeProducerDelay(ActionEvent even)
    {

    }

    @FXML
    private void randomizeSupplierDelay(ActionEvent even)
    {

    }
}
