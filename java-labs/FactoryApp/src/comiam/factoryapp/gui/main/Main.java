package comiam.factoryapp.gui.main;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.gui.fxml.MainWindowController;
import comiam.factoryapp.gui.uicore.UICore;
import comiam.factoryapp.log.Log;
import javafx.application.Application;
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

        primaryStage.setOnCloseRequest(windowEvent -> UICore.closeUIThread());
        primaryStage.show();
        controller.initSliders();
        controller.setRootStage(primaryStage);
    }


    public static void main(String[] args)
    {
        launch(args);
        /*Log.init();
        Log.enableInfoLogging();
        Factory factory = new Factory();
        factory.init(3, 10, 3,100, 30, 50, 10, 10, 10, 10);*/
    }
}
