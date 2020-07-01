package comiam.chat.client.gui;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.gui.fxml.EnterController;
import comiam.chat.client.gui.fxml.MainMenuController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static comiam.chat.client.gui.Dialogs.showExceptionDialog;

public class PaneLoader
{
    public static VBox getMessagePane()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(PaneLoader.class.getResource("../gui/fxml/messagepane.fxml"));
            Node root = loader.load();

            return (VBox) root;
        }catch(Throwable e)
        {
            return null;
        }
    }

    public static void showEnterDialog()
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader(PaneLoader.class.getResource("../gui/fxml/enter.fxml"));
            Parent root = loader.load();
            EnterController controller = loader.getController();
            controller.setStage(newWindow);

            newWindow.setTitle("Hello");
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root, 226, 296));
            newWindow.centerOnScreen();
            newWindow.show();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }

    }

    public static void showMainMenu(String name)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader(PaneLoader.class.getResource("../gui/fxml/mainmenu.fxml"));
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

            if(!MainMenuController.isLoadedSuccessfully())
                newWindow.close();
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
            Platform.exit();
            System.exit(1);
        }

    }
}
