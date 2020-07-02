package comiam.chat.client.gui;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.gui.fxml.EnterController;
import comiam.chat.client.gui.fxml.MainMenuController;
import comiam.chat.client.time.Timer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static comiam.chat.client.gui.Dialogs.centerChild;
import static comiam.chat.client.gui.Dialogs.showExceptionDialog;

public class PaneLoader
{
    public static VBox getMessagePane()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EnterController.class.getResource("messagepane.fxml"));
            Node root = loader.load();

            return (VBox) root;
        }catch(Throwable e)
        {
            return null;
        }
    }

    public static VBox getUserListPane()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EnterController.class.getResource("userlist.fxml"));
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

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EnterController.class.getResource("enter.fxml"));
            Parent root = loader.load();
            EnterController controller = loader.getController();
            controller.setStage(newWindow);

            newWindow.setTitle("Hello");
            newWindow.setResizable(false);
            newWindow.getIcons().add(new Image(EnterController.class.getResource("ico.png").toExternalForm()));
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

    public static MainMenuController showMainMenu(String name)
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EnterController.class.getResource("mainmenu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setStage(newWindow);

            newWindow.setTitle("Hello, " + name + ":)");
            newWindow.setResizable(false);
            newWindow.getIcons().add(new Image(EnterController.class.getResource("ico.png").toExternalForm()));
            newWindow.setScene(new Scene(root, 280, 400));
            newWindow.centerOnScreen();
            newWindow.setOnCloseRequest((e) ->
            {
                if(Timer.isRunning())
                    Timer.stop();
                ClientServer.disconnect();
                Platform.exit();
            });
            newWindow.show();

            if(!controller.isLoadedSuccessfully())
            {
                newWindow.close();
                if(Timer.isRunning())
                    Timer.stop();
                Platform.exit();
                System.exit(1);
            }
            return controller;
        }catch(Throwable e)
        {
            showExceptionDialog(null, e);
        }

        if(Timer.isRunning())
            Timer.stop();
        Platform.exit();
        System.exit(1);
        return null;
    }

    public static void showAboutDevelopersDialog(Stage rootStage)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EnterController.class.getResource("aboutdevs.fxml"));
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
}
