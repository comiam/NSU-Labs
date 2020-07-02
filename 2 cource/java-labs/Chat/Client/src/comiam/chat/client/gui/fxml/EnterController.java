package comiam.chat.client.gui.fxml;

import comiam.chat.client.gui.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static comiam.chat.client.connection.ClientServer.authorize;
import static comiam.chat.client.connection.ClientServer.connectToServer;

public class EnterController
{
    @FXML
    private TextField usernameTF;

    @FXML
    private TextField passwordTF;

    private Stage stage;

    public void setStage(Stage parent)
    {
        stage = parent;
    }

    @FXML
    private void signIn()
    {
        if(usernameTF.getText().trim().isEmpty() || passwordTF.getText().trim().isEmpty())
        {
            Dialogs.showDefaultAlert(stage, "Oops", "Enter correct field  values!", Alert.AlertType.ERROR);
            return;
        }

        if(!connectToServer(stage, true))
            return;
        authorize(stage, true, false, usernameTF.getText().trim(), passwordTF.getText().trim());
    }

    @FXML
    private void signUp()
    {
        if(usernameTF.getText().trim().isEmpty() || passwordTF.getText().trim().isEmpty())
        {
            Dialogs.showDefaultAlert(stage, "Oops", "Enter correct field values!", Alert.AlertType.ERROR);
            return;
        }

        if(!connectToServer(stage, true))
            return;
        authorize(stage, false, false, usernameTF.getText().trim(), passwordTF.getText().trim());
    }
}
