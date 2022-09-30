package comiam.factoryapp.gui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Dialogs
{
    public static void showDefaultAlert(Stage root, String title, String message, Alert.AlertType type, String... header)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);

        if(root != null)
            centerChild((Stage) alert.getDialogPane().getScene().getWindow(), root);

        alert.setHeaderText(header != null && header.length == 1 ? header[0] : null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void showExceptionDialog(Stage root, Throwable ex, String... message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка!");
        alert.setHeaderText("К сожалению, произошла ошибка в приложении :(" + (message.length > 0 ? "\n" + message[0] : ""));
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Стэк ошибки:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        if(root != null)
            centerChild((Stage) alert.getDialogPane().getScene().getWindow(), root);

        alert.showAndWait();
    }

    public static void centerChild(Stage child, Stage parent)
    {
        double centerXPosition = parent.getX() + parent.getWidth() / 2d;
        double centerYPosition = parent.getY() + parent.getHeight() / 2d;

        child.setOnShown(ev -> {
            child.setX(centerXPosition - child.getWidth()/2d);
            child.setY(centerYPosition - child.getHeight()/2d);
        });
    }
}
