package comiam.chat.client.gui.fxml;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.EnterDialog;
import comiam.chat.client.utils.Pair;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;

import static comiam.chat.client.gui.Dialogs.showDefaultAlert;
import static comiam.chat.client.gui.Dialogs.showInputDialog;

public class MainMenuController
{
    @FXML
    private VBox chatBox;

    private TextField messageTextField;

    private Stage stage;

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    @FXML
    public void initialize()
    {
        ArrayList<Pair<String, Integer>> list = ClientServer.getChatLists(stage);
        if(list == null)
        {
            stage.close();
            ClientServer.clearData();
            EnterDialog.show();
            return;
        }

        for(var chat : list)
            addNewChat(chat.getFirst(), chat.getSecond());
    }

    private void openChat(ArrayList<Message> messages)
    {

    }

    private void addNewChat(String name, int count)
    {
        HBox chatHB = new HBox();
        chatHB.setMinWidth(280);
        chatHB.setMinHeight(50);
        chatHB.setOnMouseClicked((e) -> {
            chatHB.setBackground(null);
            ArrayList<String> users = ClientServer.getUserFromChat(stage, name);
            if(users == null)
            {
                showDefaultAlert(stage, "Oops", "Can't get user list, sorry...", Alert.AlertType.ERROR);
                return;
            }

            boolean connected = false;

            for(var usr : users)
                if(usr.equals(LocalData.getUsername()))
                {
                    connected = true;
                    break;
                }

            if(!connected)
            {
                connected = ClientServer.connectToServer(stage);
                if(!connected)
                {
                    showDefaultAlert(stage, "Oops", "Can't connect to chat!", Alert.AlertType.ERROR);
                    return;
                }
            }

            ArrayList<Message> messages = ClientServer.getMessagesFromChat(stage, name);
            if(messages == null)
            {
                showDefaultAlert(stage, "Oops", "Can't get messages list, sorry...", Alert.AlertType.ERROR);
                return;
            }

            openChat(messages);
        });
        chatHB.setAlignment(Pos.CENTER_LEFT);

        Label nameL = new Label(name);
        nameL.setMinWidth(140);
        nameL.setMaxWidth(140);
        nameL.setWrapText(false);
        nameL.setFont(Font.font(nameL.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
        Label userCount = new Label(count + " users");
        chatHB.getChildren().addAll(nameL, userCount);
        chatHB.setOnMouseEntered((e) -> chatHB.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))));
        chatHB.setOnMousePressed((e) -> chatHB.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY))));
        chatHB.setOnMouseExited((e) -> chatHB.setBackground(null));

        chatBox.getChildren().add(chatHB);
    }

    @FXML
    private void addChat()
    {
        String name = null;

        while(name == null)
        {
            name = showInputDialog(stage, "Dialog", "lol", "Please input ip address of server with port:");
            if(name == null)
                return;

            boolean created = ClientServer.createChat(stage, name);

            if(created)
                addNewChat(name, 1);
            else
                showDefaultAlert(stage, "Oops", "Can't create new chat :c", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void exitFromApp()
    {

    }

    @FXML
    private void exitFromAcc()
    {

    }

    @FXML
    private void showInfo()
    {

    }
}
