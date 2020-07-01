package comiam.chat.client.gui;

import comiam.chat.client.gui.fxml.EnterController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EnterDialog
{
    public static void show()
    {
        try
        {
            Stage newWindow = new Stage();

            FXMLLoader loader = new FXMLLoader(EnterDialog.class.getResource("../gui/fxml/enter.fxml"));
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
            Platform.exit();
            System.exit(1);
        }

    }
}
