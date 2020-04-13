package comiam.factoryapp.gui.fxml;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.dialogs.Dialogs;
import comiam.factoryapp.gui.uicore.UICore;
import comiam.factoryapp.factory.factory.FactoryIO;
import comiam.factoryapp.io.Log;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

import static comiam.factoryapp.gui.dialogs.Dialogs.centerChild;

public class MainWindowController
{
    public enum FactoryStatus
    {
        RUNNING,
        DESTROYED,
        NOT_CREATED
    }

    @FXML
    private Slider sdSlider;

    @FXML
    private Slider pdSlider;

    @FXML
    private Slider ddSlider;

    @FXML
    private Label status;

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
    private Menu loggingOnMenu;

    @FXML
    private CheckBox loggingEnabled;

    @FXML
    private Button randomSD;

    @FXML
    private Button randomDD;

    @FXML
    private Button randomPD;

    @FXML
    private TextArea logTextArea;

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

    private long factoryID = Long.MIN_VALUE;

    @FXML
    private void selectLogging()
    {
        if(loggingEnabled.isSelected())
        {
            try
            {
                UICore.getFactory().enableLogging();
                if(factoryID != Long.MIN_VALUE && factoryID == UICore.getFactory().hashCode())
                    Log.info("=======================LOGGING ENABLED IN SAME FACTORY========================");
                else if(factoryID != Long.MIN_VALUE)
                    Log.info("======================LOGGING ENABLED IN ANOTHER FACTORY======================");
            } catch(Exception e)
            {
                Dialogs.showExceptionDialog(rootStage, e);
            }
        }else
        {
            Log.info("================================LOGGING CLOSED================================");
            if(UICore.getFactory() != null)
            {
                UICore.getFactory().disableLogging();
                factoryID = UICore.getFactory().hashCode();
            }
            else
            {
                Log.disableInfoLogging();
                Log.disableLogging();
            }
        }
    }

    @FXML
    private void quit()
    {
        loggingEnabled.setSelected(false);
        selectLogging();

        UICore.disableFactory();
        Platform.exit();
    }

    private void checkLogging()
    {
        if(loggingEnabled.isDisabled())
        {
            Dialogs.showDefaultAlert(rootStage, "Error", "I haven't any working factory!", Alert.AlertType.ERROR);
            return;
        }

        loggingEnabled.setSelected(!loggingEnabled.isSelected());
        selectLogging();
    }

    @FXML
    private void restartFactory()
    {
        if(UICore.factoryIsDisabled())
        {
            Dialogs.showDefaultAlert(rootStage, "Error", "I haven't any working factory!", Alert.AlertType.ERROR);
            return;
        }
        UICore.restartFactory();
    }

    @FXML
    private void stopFactory()
    {
        if(UICore.factoryIsDisabled())
        {
            Dialogs.showDefaultAlert(rootStage, "Error", "I haven't any working factory!", Alert.AlertType.ERROR);
            return;
        }

        disableAll();
        loggingEnabled.setSelected(false);
        selectLogging();
        setStatus(FactoryStatus.DESTROYED);
        printLog("Destroyed\n");

        UICore.disableFactory();
    }

    private Stage rootStage = null;

    @FXML
    private void saveFactory()
    {
        if(UICore.getFactory() == null || !UICore.getFactory().isInitialized())
        {
            Dialogs.showDefaultAlert(rootStage, "Error", "I haven't any working factory!", Alert.AlertType.ERROR);
            return;
        }

        try
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("config.fc");
            fileChooser.setTitle("Choose file to save factory configuration.");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Factory configuration (*.fc)",
                    "*.fc");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(rootStage);
            if (file == null)
                return;

            if(!FactoryIO.saveFactory(file, UICore.getFactory()))
                Dialogs.showDefaultAlert(rootStage, "Error", "Can't save file. Unexpected error!", Alert.AlertType.ERROR);
        } catch (Throwable e)
        {
            Dialogs.showExceptionDialog(rootStage, e);
        }
    }

    @FXML
    private void loadFactory()
    {
        try
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose file to open factory configuration.");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Factory configuration (*.fc)",
                    "*.fc");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(rootStage);

            if (file == null)
                return;

            Factory factory = FactoryIO.readFactory(file);
            if(factory == null)
                Dialogs.showDefaultAlert(rootStage, "Error", "Bad XML Configuration file!", Alert.AlertType.ERROR);
            else
                UICore.enableFactoryProcess(factory);
        } catch (Throwable e)
        {
            Dialogs.showExceptionDialog(rootStage, e);
        }
    }

    @FXML
    private void showAboutDevelopers()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWindowController.class.getResource("AboutDevelopersPane.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("About developer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(rootStage);

            if(rootStage != null)
                centerChild(dialogStage, rootStage);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            dialogStage.show();
        } catch (Throwable e)
        {
            Dialogs.showExceptionDialog(rootStage, e);
        }
    }

    @FXML
    private void showManualStartWindow()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWindowController.class.getResource("ManStartWindow.fxml"));
            AnchorPane page = loader.load();
            ManStartWindowController controllerN = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Input factory configuration!");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(rootStage);

            if(rootStage != null)
                centerChild(dialogStage, rootStage);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            controllerN.setStage(dialogStage);
            dialogStage.showAndWait();
        } catch (Throwable e)
        {
            Dialogs.showExceptionDialog(rootStage, e);
        }
    }

    @FXML
    public void initialize()
    {
        UICore.initCore(this);
        logTextArea.textProperty().addListener((observable, oldValue, newValue) ->
        {
            while (logTextArea.getText().split("\n", -1).length > 26)
            {
                int fle = logTextArea.getText().indexOf("\n");
                logTextArea.replaceText(0, fle + 1, "");
            }
            logTextArea.positionCaret(logTextArea.getText().length());
        });

        loggingOnMenu.setStyle("-fx-effect: none; -fx-background-color: WHITE;");
        setStatus(FactoryStatus.NOT_CREATED);
        disableAll();
    }

    public void setRootStage(Stage rootStage)
    {
        this.rootStage = rootStage;
        rootStage.getScene().setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.L && e.isAltDown())
                checkLogging();
        });
    }

    public Stage getRootStage()
    {
        return rootStage;
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

    public void setDDSliderVal(int val)
    {
        ddSlider.setValue(val);
    }

    public void setSDSliderVal(int val)
    {
        sdSlider.setValue(val);
    }

    public void setPDSliderVal(int val)
    {
        pdSlider.setValue(val);
    }

    private void changeRandomSlideVal(Slider slider)
    {
        var tl = new Timeline();

        tl.setCycleCount(1);
        tl.setAutoReverse(false);

        double var = Factory.randomizeDelay() * 1000;
        var -= var % 10;
        if(var == 0)
            var = 10;

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

    public void clearFields()
    {
        setWorkingTime("00:00:00");
        setFactoryLoad("0%");
        setCarsMade(0);
        setCarsSend(0);
        setCarStoreCount(0);
        setEngineDelivered(0);

        setBodyworkDelivered(0);
        setAccessoryDelivered(0);
        setBodyworkStoreCount(0);
        setAccessoryStoreCount(0);
        setEngineStoreCount(0);
        setSupplierCount(0);

        setProducerCount(0);
        setDealerCount(0);
    }

    public void setStatus(FactoryStatus fstatus)
    {
        switch(fstatus)
        {
            case RUNNING:
                status.setText("Running");
                break;
            case DESTROYED:
                status.setText("Destroyed");
                break;
            case NOT_CREATED:
                status.setText("Not created");
                break;
        }
    }

    public void enableAll()
    {
        ddSlider.setDisable(false);
        sdSlider.setDisable(false);
        pdSlider.setDisable(false);
        loggingEnabled.setDisable(false);
        randomDD.setDisable(false);
        randomPD.setDisable(false);
        randomSD.setDisable(false);
    }

    public void disableAll()
    {
        ddSlider.setDisable(true);
        sdSlider.setDisable(true);
        pdSlider.setDisable(true);
        loggingEnabled.setDisable(true);
        randomDD.setDisable(true);
        randomPD.setDisable(true);
        randomSD.setDisable(true);
    }

    public void setCBLogging(boolean checked)
    {
        loggingEnabled.setSelected(checked);
    }


    public synchronized void printLog(String message)
    {
        if(!logTextArea.getText().isEmpty())
            logTextArea.setText(logTextArea.getText() + "\n");
        logTextArea.setText(logTextArea.getText() + message);
    }

    public void resetLog()
    {
        logTextArea.setText("");
    }
}