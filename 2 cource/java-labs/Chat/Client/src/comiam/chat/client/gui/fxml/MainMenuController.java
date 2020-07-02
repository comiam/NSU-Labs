package comiam.chat.client.gui.fxml;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.PaneLoader;
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
    private Button addChatButton;

    @FXML
    private Button updateButton;

    @FXML
    private VBox chatPanel;

    @FXML
    private VBox mainPanel;

    @FXML
    private VBox chatBox;

    private VBox msgBox;

    private Stage stage;

    private TextArea messageArea;

    private String openedChatName = null;

    private boolean loadedSuccessfully = false;

    private ArrayList<Message> currentMessages;

    public void appendNewMessages(ArrayList<Message> newPack)
    {
        if(openedChatName == null)
            return;

        if(currentMessages.size() >= newPack.size())
            return;

        for(int i = currentMessages.size();i < newPack.size();i++)
        {
            currentMessages.add(newPack.get(i));
            int finalI = i;
            Platform.runLater(() -> messageArea.appendText(newPack.get(finalI).getUsername() + "(" + newPack.get(finalI).getDate() + "): " + newPack.get(finalI).getText() + "\n"));
        }
    }


    public String getOpenedChatName()
    {
        return openedChatName;
    }

    public boolean isLoadedSuccessfully()
    {
        return loadedSuccessfully;
    }

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

    public void loadChatList(ArrayList<Pair<String, Integer>> list)
    {
        Platform.runLater(() -> {
            chatBox.getChildren().clear();

            for(var chat : list)
                addNewChat(chat.getFirst(), chat.getSecond());
        });
    }

    private void loadChatList(boolean onStart)
    {
        ArrayList<Pair<String, Integer>> list = ClientServer.getChatLists(stage);
        if(list == null)
        {
            if(onStart)
            {
                ClientServer.clearData(true);
                PaneLoader.showEnterDialog();
            } else
                showDefaultAlert(stage, "Oops", "I can't update chat list :c", Alert.AlertType.ERROR);
            return;
        }
        loadedSuccessfully = true;

        chatBox.getChildren().clear();

        for(var chat : list)
            addNewChat(chat.getFirst(), chat.getSecond());
    }

    private void openChat(ArrayList<Message> messages, String nameChat)
    {
        updateButton.setVisible(false);
        addChatButton.setOnAction((e) -> exitFromChat());
        addChatButton.setText("exit");

        msgBox = PaneLoader.getMessagePane();
        assert msgBox != null;
        msgBox.setPrefHeight(chatPanel.getPrefHeight());
        msgBox.setPrefWidth(chatPanel.getPrefWidth());
        msgBox.setMinHeight(chatPanel.getMinHeight());
        msgBox.setMinWidth(chatPanel.getMinWidth());
        msgBox.setMaxHeight(chatPanel.getMaxHeight());
        msgBox.setMinWidth(chatPanel.getMinWidth());

        TextArea area = (TextArea) ((ScrollPane) msgBox.getChildren().get(0)).getContent();
        TextField field = (TextField) msgBox.getChildren().get(1);

        area.setFont(Font.font(area.getFont().getFamily(), FontPosture.REGULAR, 12));
        for(var msg : messages)
            area.appendText(msg.getUsername() + "(" + msg.getDate() + "): " + msg.getText() + "\n");

        field.setPromptText("input message...");
        field.setMaxWidth(Double.MAX_VALUE);
        field.setMinHeight(20);
        field.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.ENTER && !field.getText().trim().isEmpty())
            {
                if(!ClientServer.sendMessage(stage, nameChat, field.getText().trim()))
                    showDefaultAlert(stage, "Oops", "Can't send message to server... Try again!", Alert.AlertType.ERROR);
                else
                {
                    String date = Date.getDate();
                    area.appendText(LocalData.getUsername() + "(" + date + "): " + field.getText().trim() + "\n");
                    currentMessages.add(new Message(field.getText().trim(), date, LocalData.getUsername()));
                    field.setText("");
                }
            }
        });
        mainPanel.getChildren().remove(chatPanel);
        mainPanel.getChildren().add(msgBox);

        messageArea = area;
        openedChatName = nameChat;
        currentMessages = messages;
    }

    private void exitFromChat()
    {
        updateButton.setVisible(true);
        addChatButton.setOnAction((e) -> addChat());
        addChatButton.setText("+");

        mainPanel.getChildren().remove(msgBox);
        mainPanel.getChildren().add(chatPanel);

        messageArea = null;
        openedChatName = null;
        currentMessages = null;
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
                boolean connected = ClientServer.connectToChat(stage, name);
                if(!connected)
                {
                    showDefaultAlert(stage, "Oops", "Can't connect to chat!", Alert.AlertType.ERROR);
                    return;
                }
                loadChatList(false);


                users = ClientServer.getUserFromChat(stage, name);
                if(users == null)
                {
                    showDefaultAlert(stage, "Oops", "Can't get user list, sorry...", Alert.AlertType.ERROR);
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
        ClientServer.disconnect();
        ClientServer.clearData(true);
        stage.close();
        PaneLoader.showEnterDialog();
    }

    @FXML
    private void showInfo()
    {
        PaneLoader.showAboutDevelopersDialog(stage);
    }
}
