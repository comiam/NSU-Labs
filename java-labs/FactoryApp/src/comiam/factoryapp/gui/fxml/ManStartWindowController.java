package comiam.factoryapp.gui.fxml;

import comiam.factoryapp.gui.dialogs.Dialogs;
import comiam.factoryapp.gui.uicore.UICore;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import static comiam.factoryapp.util.Numbers.getInteger;
import static comiam.factoryapp.util.Numbers.getIntegerWithCondition;

public class ManStartWindowController
{
    @FXML
    private TextField accessorySupplierCountTF;

    @FXML
    private TextField producerCountTF;

    @FXML
    private TextField dealerCountTF;

    @FXML
    private TextField supplierDelayTF;

    @FXML
    private TextField producerDelayTF;

    @FXML
    private TextField dealerDelayTF;

    @FXML
    private TextField accessoryStoreCapacityTF;

    @FXML
    private TextField bodyworkStoreCapacityTF;

    @FXML
    private TextField engineStoreCapacityTF;

    @FXML
    private TextField carStoreCapacityTF;

    @FXML
    private CheckBox  enableLoggingCBox;

    private Stage dialogStage;

    public void setStage(Stage stage)
    {
        this.dialogStage = stage;
        dialogStage.getScene().setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.ENTER)
                setData();
        });
    }

    @FXML
    private void setData()
    {
        if(checkArgs(accessorySupplierCountTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid accessory supplier count value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(producerCountTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid producer count value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(dealerCountTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid dealer count value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(supplierDelayTF, true))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid supplier delay value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(producerDelayTF, true))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid producer delay value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(dealerDelayTF, true))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid dealer delay value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(accessoryStoreCapacityTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid accessory store capacity value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(engineStoreCapacityTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid engine store capacity value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(bodyworkStoreCapacityTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid bodywork store capacity value!", Alert.AlertType.ERROR);
            return;
        }

        if(checkArgs(carStoreCapacityTF, false))
        {
            Dialogs.showDefaultAlert(dialogStage, "Error!", "Invalid car store capacity value!", Alert.AlertType.ERROR);
            return;
        }

        dialogStage.close();

        UICore.enableFactoryProcess(
                getIntFromField(accessorySupplierCountTF), getIntFromField(producerCountTF), getIntFromField(dealerCountTF),
                getIntFromField(supplierDelayTF), getIntFromField(producerDelayTF), getIntFromField(dealerDelayTF),
                getIntFromField(accessoryStoreCapacityTF), getIntFromField(engineStoreCapacityTF),
                getIntFromField(bodyworkStoreCapacityTF), getIntFromField(carStoreCapacityTF),
                enableLoggingCBox.isSelected());
    }

    private Integer getIntFromField(TextField field)
    {
        return getInteger(field.getText().isEmpty() ? field.getPromptText() : field.getText());
    }

    private boolean checkArgs(TextField field, boolean checkOnNegative)
    {
        return getIntegerWithCondition(field.getText().isEmpty() ? field.getPromptText() : field.getText(), (integer -> ((checkOnNegative ? integer >= 10 : integer > 0) && integer <= 2000) || (checkOnNegative && integer == -1))) == null;
    }
}
