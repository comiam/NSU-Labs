package comiam.chat.client.gui.fxml;

import comiam.chat.client.gui.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static comiam.chat.client.connection.ClientServer.authorize;
import static comiam.chat.client.connection.ClientServer.connectToServer;
import static comiam.chat.client.gui.Dialogs.showDefaultAlert;

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

        if(usernameTF.getText().trim().length() > 20)
        {
            showDefaultAlert(stage, "Oops", "Username length must be less than 20!", Alert.AlertType.ERROR);
            return;
        }

        if(passwordTF.getText().trim().length() > 32)
        {
            showDefaultAlert(stage, "Oops", "Password length must be less than 32!", Alert.AlertType.ERROR);
            return;
        }

        if(!connectToServer(stage, true))
            return;
        authorize(stage, true, false, true, usernameTF.getText().trim(), passwordTF.getText().trim());
    }

    @FXML
    private void signUp()
    {
        if(usernameTF.getText().trim().isEmpty() || passwordTF.getText().trim().isEmpty())
        {
            Dialogs.showDefaultAlert(stage, "Oops", "Enter correct field values!", Alert.AlertType.ERROR);
            return;
        }

        if(usernameTF.getText().trim().length() > 20)
        {
            showDefaultAlert(stage, "Oops", "Username length must be less than 20!", Alert.AlertType.ERROR);
            return;
        }

        if(passwordTF.getText().trim().length() > 32)
        {
            showDefaultAlert(stage, "Oops", "Password length must be less than 32!", Alert.AlertType.ERROR);
            return;
        }

        if(!connectToServer(stage, true))
            return;
        authorize(stage, false, false, true, usernameTF.getText().trim(), passwordTF.getText().trim());
    }
}
