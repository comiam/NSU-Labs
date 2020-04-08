package comiam.factoryapp.gui.fxml;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.dialogs.Dialogs;
import comiam.factoryapp.gui.uicore.UICore;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.PrintWriter;
import java.io.StringWriter;

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
    private Label cStoreCountLabel;

    @FXML
    private Label dealerCountLabel;

    @FXML
    private Label producerCountLabel;

    @FXML
    private Label supplierCountLabel;

    @FXML
    private TextField logTextField;

    @FXML
    private void randomizeDealerDelay()
    {
        changeRandomSlideVal(ddSlider);
    }

    @FXML
    private void randomizeProducerDelay()
    {
        changeRandomSlideVal(pdSlider);
    }

    @FXML
    private void randomizeSupplierDelay()
    {
        changeRandomSlideVal(sdSlider);
    }

    @FXML
    private void quit()
    {
        Platform.exit();
    }

    private Stage rootStage = null;

    @FXML
    private void showManualStartWindow()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWindowController.class.getResource("ManStartWindow.fxml"));
            VBox page = loader.load();
            ManStartWindowController controllerN = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Задайте настройки!");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(rootStage);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (Throwable e)
        {
            Dialogs.showExceptionDialog(e);
        }
    }

    public void setRootStage(Stage rootStage)
    {
        this.rootStage = rootStage;
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
            if(UICore.getFactory() != null)
            {
                synchronized(UICore.getFactory())
                {
                    if(slider.equals(ddSlider))
                        UICore.getFactory().setDealerDelay(newVal.intValue() - newVal.intValue() % 10);
                    else if(slider.equals(sdSlider))
                        UICore.getFactory().setSupplierDelay(newVal.intValue() - newVal.intValue() % 10);
                    else if(slider.equals(pdSlider))
                        UICore.getFactory().setProducerDelay(newVal.intValue() - newVal.intValue() % 10);
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

    public void setWorkingTime(String time)
    {
        workingTimeLabel.setText(time);
    }

    public void setFactoryLoad(String load)
    {
        factoryLoadLabel.setText(load);
    }

    public void setCarsMade(long count)
    {
        carsMadeLabel.setText("" + count);
    }

    public void setCarsSend(long count)
    {
        carsSuppliedLabel.setText("" + count);
    }

    public void setEngineDelivered(long count)
    {
        engineDeliveredLabel.setText("" + count);
    }

    public void setAccessoryDelivered(long count)
    {
        accessoryDeliveredLabel.setText("" + count);
    }

    public void setBodyworkDelivered(long count)
    {
        bodyworkDeliveredLabel.setText("" + count);
    }

    public void setBodyworkStoreCount(long count)
    {
        bStoreCountLabel.setText("" + count);
    }

    public void setAccessoryStoreCount(long count)
    {
        aStoreCountLabel.setText("" + count);
    }

    public void setEngineStoreCount(long count)
    {
        eStoreCountLabel.setText("" + count);
    }

    public void setCarStoreCount(long count)
    {
        cStoreCountLabel.setText("" + count);
    }

    public void setDealerCount(long count)
    {
        dealerCountLabel.setText("" + count);
    }

    public void setProducerCount(long count)
    {
        producerCountLabel.setText("" + count);
    }

    public void setSupplierCount(long count)
    {
        supplierCountLabel.setText("" + count);
    }

    public void printLog(String message)
    {
        logTextField.appendText(message);
    }

    public void resetLog()
    {
        logTextField.setText("");
    }
}