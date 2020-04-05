package comiam.factoryapp.gui.main;

import comiam.factoryapp.factory.factory.Factory;
import comiam.factoryapp.log.Log;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../fxml/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args)
    {
        //launch(args);
        Log.init();
        Log.enableInfoLogging();
        Factory factory = new Factory();
        factory.init(3, 10, 3,100, 30, 50, 10, 10, 10, 10);
    }
}
