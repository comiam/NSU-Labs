package comiam.chat.client.gui;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.gui.fxml.MainMenuController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static comiam.chat.client.gui.Dialogs.showExceptionDialog;

public class MainMenu
{
    public static void show(String name)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader(EnterDialog.class.getResource("../gui/fxml/mainmenu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setStage(newWindow);

            newWindow.setTitle("Hello, " + name + ":)");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 280, 400));
            newWindow.centerOnScreen();
            newWindow.setOnCloseRequest((e) ->
            {
                ClientServer.disconnect();
                Platform.exit();
            });
            newWindow.show();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }

    }
}
