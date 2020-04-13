package comiam.factoryapp.gui.main;

import comiam.factoryapp.gui.fxml.MainWindowController;
import comiam.factoryapp.gui.uicore.UICore;
import comiam.factoryapp.time.Timer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../fxml/MainWindow.fxml"));
        Parent root = loader.load();
        MainWindowController controller = loader.getController();

        primaryStage.setTitle("Factory Manager");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image(getClass().getResource("/res/zavod.png").toExternalForm()));
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest((e) ->
        {
            UICore.disableFactory();
            Platform.exit();
        });
        primaryStage.show();
        controller.initSliders();
        controller.setRootStage(primaryStage);
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
