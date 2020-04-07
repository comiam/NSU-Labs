package comiam.factoryapp.gui.fxml;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.uithread.ThreadUpdater;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class MainWindowController
{
    @FXML
    private Slider sdSlider;

    @FXML
    private Slider pdSlider;

    @FXML
    private Slider ddSlider;

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
    private Label bStoreCountLabel;

    @FXML
    private Label aStoreCountLabel;

    @FXML
    private Label eStoreCountLabel;

    @FXML
    private Label dealerCountLabel;

    @FXML
    private Label producerCountLabel;

    @FXML
    private Label supplierCountLabel;

    private Factory factory = null;
    private Thread  uiThread = null;

    public void setFactory(Factory factory)
    {
        this.factory = factory;
    }

    public void runUIThread()
    {
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
    }

    @FXML
    private void randomizeDealerDelay(ActionEvent event)
    {
        changeRandomSlideVal(ddSlider);
    }

    @FXML
    private void randomizeProducerDelay(ActionEvent event)
    {
        changeRandomSlideVal(pdSlider);
    }

    @FXML
    private void randomizeSupplierDelay(ActionEvent event)
    {
        changeRandomSlideVal(sdSlider);
    }

    @FXML
    private void quit(ActionEvent event)
    {
        closeUIThread();
        Platform.exit();
    }

    public void initSliders()
    {
        setSliderListener(ddSlider);
        setSliderListener(sdSlider);
        setSliderListener(pdSlider);
    }

    private void setSliderListener(Slider slider)
    {
        Pane p = (Pane) slider.lookup(".thumb");

        Label l = new Label();
        l.setFont(Font.font("Arial", FontWeight.LIGHT, 11));
        l.textProperty().bind(slider.valueProperty().asString("%1.0f"));
        l.setTextAlignment(TextAlignment.CENTER);
        l.setAlignment(Pos.CENTER);

        p.getChildren().add(l);
        p.setPrefSize(40, 20);

        slider.valueProperty().addListener((obs, oldval, newVal) ->
        {
            slider.setValue(newVal.doubleValue() - newVal.doubleValue() % 10);
            if(factory != null)
            {
                synchronized(factory)
                {
                    if(slider.equals(ddSlider))
                        factory.setDealerDelay(newVal.intValue() - newVal.intValue() % 10);
                    else if(slider.equals(sdSlider))
                        factory.setSupplierDelay(newVal.intValue() - newVal.intValue() % 10);
                    else if(slider.equals(pdSlider))
                        factory.setProducerDelay(newVal.intValue() - newVal.intValue() % 10);
                }
            }
        });
    }
    private void changeRandomSlideVal(Slider slider)
    {
        var tl = new Timeline();

        tl.setCycleCount(1);
        tl.setAutoReverse(false);

        double var = Factory.randomizeDelay() * 1000;
        var -= var % 10;

        var kv = new KeyValue(slider.valueProperty(), var, Interpolator.EASE_BOTH);
        var kf = new KeyFrame(Duration.millis(150), kv);
        tl.getKeyFrames().addAll(kf);

        tl.play();
    }
}