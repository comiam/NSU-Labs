package comiam.chat.client.main;

import comiam.chat.client.gui.EnterDialog;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage)
    {
        EnterDialog.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
