package comiam.chat.client.gui.fxml;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.EnterDialog;
import comiam.chat.client.time.Date;
import comiam.chat.client.utils.Pair;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
    private ScrollPane parentScroll;

    @FXML
    private Button addChatButton;

    @FXML
    private Button updateButton;

    @FXML
    private VBox chatBox;

    @FXML
    private VBox messageBox;

    private TextArea messageTextField;

    private Stage stage;

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    @FXML
    public void initialize()
    {
        addChatButton.setOnAction((e) -> addChat());
        updateButton.setOnAction((e) -> loadChatList(false));

        loadChatList(true);
    }

    private void loadChatList(boolean onStart)
    {
        ArrayList<Pair<String, Integer>> list = ClientServer.getChatLists(stage);
        if(list == null)
        {
            if(onStart)
            {
                stage.close();
                ClientServer.clearData();
                EnterDialog.show();
            }else
                showDefaultAlert(stage, "Oops", "I can't update chat list :c", Alert.AlertType.ERROR);
            return;
        }

        chatBox.getChildren().clear();

        for(var chat : list)
            addNewChat(chat.getFirst(), chat.getSecond());
    }

    private void openChat(ArrayList<Message> messages, String nameChat)
    {
        updateButton.setVisible(false);
        addChatButton.setOnAction((e)->exitFromChat());
        addChatButton.setText("exit");

        parentScroll.getChildrenUnmodifiable().remove(chatBox);

        messageBox = new VBox();
        messageBox.setMinHeight(350);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        messageTextField = new TextArea();
        messageTextField.setMaxHeight(Double.MAX_VALUE);
        messageTextField.setMaxWidth(330);
        messageTextField.setFont(Font.font(messageTextField.getFont().getFamily(), FontPosture.REGULAR, 14));
        for(var msg : messages)
            messageTextField.appendText(msg.getUser().getUsername() + "(" + msg.getDate() + "): " + msg.getText() + "\n");

        messageBox.getChildren().add(messageTextField);

        TextField messageField = new TextField();
        messageField.setPromptText("input message...");
        messageField.setMaxWidth(Double.MAX_VALUE);
        messageField.setMinHeight(20);
        messageField.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.ENTER && !messageField.getText().trim().isEmpty())
            {
                if(!ClientServer.sendMessage(stage, nameChat, messageField.getText().trim()))
                    showDefaultAlert(stage, "Oops", "Can't send message to server... Try again!", Alert.AlertType.ERROR);
                else
                {
                    messageTextField.appendText(LocalData.getUsername() + "(" + Date.getDate() + "): " + messageField.getText().trim() + "\n");
                    messageField.setText("");
                }
            }
        });
        messageBox.getChildren().add(messageField);

        parentScroll.setContent(messageBox);
    }

    private void exitFromChat()
    {
        updateButton.setVisible(true);
        addChatButton.setOnAction((e)->addChat());
        addChatButton.setText("+");

        parentScroll.getChildrenUnmodifiable().remove(messageBox);
        parentScroll.setContent(chatBox);
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
            {
                System.out.println(usr);
                if(usr.equals(LocalData.getUsername()))
                {
                    connected = true;
                    break;
                }
            }
            if(!connected)
            {
                connected = ClientServer.connectToChat(stage, name);
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

            openChat(messages, name);
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
        ClientServer.disconnect();
        Platform.exit();
        System.exit(0);
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
