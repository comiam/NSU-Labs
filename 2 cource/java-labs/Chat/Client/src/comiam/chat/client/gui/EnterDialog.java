package comiam.chat.client.gui;

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

            Parent root = FXMLLoader.load(EnterDialog.class.getResource("../gui/fxml/enter.fxml"));
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
