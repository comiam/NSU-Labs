package comiam.chat.client.gui.fxml;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.connection.Connection;
import comiam.chat.client.connection.message.ErrorType;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.PaneLoader;
import comiam.chat.client.time.Date;
import comiam.chat.client.time.Timer;
import comiam.chat.client.utils.Pair;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.ArrayList;

import static comiam.chat.client.gui.Dialogs.showDefaultAlert;
import static comiam.chat.client.gui.Dialogs.showInputDialog;
import static comiam.chat.client.utils.GUIUtils.*;

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

    @FXML
    private Label chatName;

    private VBox msgBox;

    private VBox userList;

    private Stage stage;
    private TextFlow messageArea;
    private ArrayList<Message> currentMessages;

    private boolean loadedSuccessfully = false;
    private boolean userListIsOpened = false;

    public boolean isUserListIsOpened()
    {
        return userListIsOpened;
    }

    public void appendNewMessages(ArrayList<Message> newPack)
    {
        if(chatName.getText().isEmpty())
            return;

        if(currentMessages.size() >= newPack.size())
            return;

        for(int i = currentMessages.size();i < newPack.size();i++)
        {
            currentMessages.add(newPack.get(i));
            int finalI = i;

            Platform.runLater(() -> addMessage(messageArea, newPack.get(finalI).getUsername(), newPack.get(finalI).getDate(), newPack.get(finalI).getText()));
        }
    }

    public String getOpenedChatName()
    {
        return chatName.getText();
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
        var list = ClientServer.getChatLists(stage);
        if(list == null)
        {
            if(onStart)
            {
                ClientServer.clearData(true);
                PaneLoader.showEnterDialog();
            } else
            {
                checkOnFallingRelogin();
                var pair = ClientServer.checkConnection();

                if(!pair.getFirst())
                    return;
                showDefaultAlert(stage, "Oops", "I can't update chat list :c", Alert.AlertType.ERROR);
            }
            return;
        }
        loadedSuccessfully = true;

        chatBox.getChildren().clear();

        for(var chat : list)
            addNewChat(chat.getFirst(), chat.getSecond());
    }

    private void exitFromUserList()
    {
        updateButton.setVisible(true);
        addChatButton.setOnAction((e) -> exitFromChat());

        mainPanel.getChildren().remove(userList);
        mainPanel.getChildren().add(msgBox);

        userList = null;
        userListIsOpened = false;
    }

    public void reloadUserList()
    {
        var userArr = ClientServer.getUserFromChat(stage, chatName.getText());
        if(userArr == null)
        {
            checkOnFallingRelogin();
            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            userArr = ClientServer.getUserFromChat(stage, chatName.getText());
            if(userArr == null)
            {
                checkOnFallingRelogin();
                return;
            }
        }

        fillUserList(userArr);
    }

    public void reloadChatList()
    {
        var list = ClientServer.getChatLists(stage);
        if(list == null)
        {
            checkOnFallingRelogin();
            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            list = ClientServer.getChatLists(stage);
            if(list == null)
            {
                checkOnFallingRelogin();
                return;
            }
        }

        loadChatList(list);
    }

    public void reloadMessageList()
    {
        var list = ClientServer.getMessagesFromChat(stage, chatName.getText());
        if(list == null)
        {
            checkOnFallingRelogin();
            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            list = ClientServer.getMessagesFromChat(stage, chatName.getText());
            if(list == null)
            {
                checkOnFallingRelogin();
                return;
            }
        }

        ArrayList<Message> finalList = list;
        currentMessages = finalList;
        Platform.runLater(() -> {
            var textFlow = (TextFlow) ((ScrollPane) msgBox.getChildren().get(0)).getContent();
            textFlow.getChildren().clear();

            for(var msg : finalList)
                addMessage(textFlow, msg.getUsername(), msg.getDate(), msg.getText());
        });
    }

    private void showUserList()
    {
        var userArr = ClientServer.getUserFromChat(stage, chatName.getText());

        if(userArr == null)
        {
            checkOnFallingRelogin();
            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            userArr = ClientServer.getUserFromChat(stage, chatName.getText());
            if(userArr == null)
            {
                checkOnFallingRelogin();
                showDefaultAlert(stage, "Oops", "Can't get user list, sorry...", Alert.AlertType.ERROR);
                return;
            }
        }
        userListIsOpened = true;

        updateButton.setVisible(false);
        addChatButton.setOnAction((e) -> exitFromUserList());

        userList = PaneLoader.getUserListPane();

        assert userList != null;
        TextFlow userFlow = getTextFlow((ScrollPane) userList.getChildren().get(1));
        ((ScrollPane) userList.getChildren().get(1)).setContent(userFlow);

        fillUserList(userArr);

        setSmartScrollProperty((ScrollPane) userList.getChildren().get(1), userFlow);

        mainPanel.getChildren().remove(msgBox);
        mainPanel.getChildren().add(userList);
    }

    public void updateUsers()
    {
        var userArr = ClientServer.getUserFromChat(stage, chatName.getText());

        if(userArr == null)
        {
            checkOnFallingRelogin();

            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            userArr = ClientServer.getUserFromChat(stage, chatName.getText());
            if(userArr == null)
            {
                checkOnFallingRelogin();
                showDefaultAlert(stage, "Oops", "Can't get user list, sorry...", Alert.AlertType.ERROR);
                return;
            }
        }

        fillUserList(userArr);
    }

    private void fillUserList(ArrayList<Pair<String, String>> userArr)
    {
        Platform.runLater(() -> {
            TextFlow users = (TextFlow) ((ScrollPane) userList.getChildren().get(1)).getContent();
            users.getChildren().clear();
            users.layout();

            for(var usr : userArr)
                addUserToList(users, usr);
        });
    }

    private void openChat(ArrayList<Message> messages, String nameChat)
    {
        updateButton.setText("users");
        updateButton.setOnAction((e) -> showUserList());

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

        TextFlow msgArea = getTextFlow((ScrollPane) msgBox.getChildren().get(0));

        ((ScrollPane) msgBox.getChildren().get(0)).setContent(msgArea);
        TextField field = (TextField) msgBox.getChildren().get(1);

        for(var msg : messages)
            addMessage(msgArea, msg.getUsername(), msg.getDate(), msg.getText());

        setSmartScrollProperty((ScrollPane) msgBox.getChildren().get(0), msgArea);

        field.setPromptText("input message...");
        field.setMaxWidth(Double.MAX_VALUE);
        field.setMinHeight(20);
        field.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.ENTER && !field.getText().trim().isEmpty())
            {
                if(field.getText().trim().length() > 1500)
                {
                    showDefaultAlert(stage, "Oops", "Message size must be less than 1500!\n Your size is " + field.getText().trim().length() + ".", Alert.AlertType.ERROR);
                    return;
                }

                if(!ClientServer.sendMessage(stage, nameChat, field.getText().trim()))
                {
                    checkOnFallingRelogin();
                    showDefaultAlert(stage, "Oops", "Can't send message to server... Try again!", Alert.AlertType.ERROR);
                }
                else
                {
                    String date = Date.getDate();
                    addMessage(msgArea, LocalData.getUsername(), date, field.getText().trim());
                    currentMessages.add(new Message(field.getText().trim(), date, LocalData.getUsername()));
                    field.setText("");
                }
            }
        });
        mainPanel.getChildren().remove(chatPanel);
        mainPanel.getChildren().add(msgBox);

        messageArea = msgArea;
        chatName.setText(nameChat);
        chatName.setVisible(true);
        currentMessages = messages;
    }

    private void exitFromChat()
    {
        chatName.setText("");
        chatName.setVisible(false);

        updateButton.setText("upd");
        updateButton.setOnAction((e) -> loadChatList(false));

        addChatButton.setOnAction((e) -> addChat());
        addChatButton.setText("+");

        mainPanel.getChildren().remove(msgBox);
        mainPanel.getChildren().add(chatPanel);

        messageArea = null;
        currentMessages = null;
    }

    private void addNewChat(String name, int count)
    {
        HBox chatHB = new HBox();
        chatHB.setMinWidth(280);
        chatHB.setMinHeight(50);
        chatHB.setOnMouseClicked((e) -> {
            chatHB.setBackground(null);

            ArrayList<Message> messages = ClientServer.getMessagesFromChat(stage, name);
            if(messages == null)
            {
                checkOnFallingRelogin();

                var pair = ClientServer.checkConnection();

                if(!pair.getFirst())
                    return;

                boolean connected = ClientServer.connectToChat(stage, name);
                if(!connected)
                {
                    checkOnFallingRelogin();
                    showDefaultAlert(stage, "Oops", "Can't connect to chat!", Alert.AlertType.ERROR);
                    return;
                }
                loadChatList(false);


                messages = ClientServer.getMessagesFromChat(stage, name);
                if(messages == null)
                {
                    showDefaultAlert(stage, "Oops", "Can't get messages list, sorry...", Alert.AlertType.ERROR);
                    return;
                }
            }

            openChat(messages, name);
        });
        chatHB.setAlignment(Pos.CENTER_LEFT);

        Label nameL = new Label(name);
        nameL.setMinWidth(140);
        nameL.setMaxWidth(140);
        nameL.setWrapText(false);
        nameL.setFont(Font.font(nameL.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 14));
        setPopup(nameL, name, false);

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
            name = showInputDialog(stage, "Dialog", "chat", "Please input chat name:");
            if(name == null)
                return;

            if(name.length() > 20)
            {
                showDefaultAlert(stage, "Oops", "Chat name size must be less than 20!", Alert.AlertType.ERROR);
                return;
            }

            boolean created = ClientServer.createChat(stage, name);

            if(created)
                addNewChat(name, 1);
            else
            {
                var pair = ClientServer.checkConnection();

                if(!pair.getFirst())
                    return;
                showDefaultAlert(stage, "Oops", "Can't create new chat :c", Alert.AlertType.ERROR);
            }
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
        Timer.stop();
        PaneLoader.showEnterDialog();
    }

    private void checkOnFallingRelogin()
    {
        if(ClientServer.getLastError() == ErrorType.USER_ALREADY_CONNECTED)
        {
            Timer.stop();
            Platform.runLater(() -> {
                showDefaultAlert(stage, "Oops", "Unfortunately, somebody authorized to your account, " +
                        "when server falling down... Sorry... Chat will close.", Alert.AlertType.ERROR);
                Connection.closeConnection();
                Platform.exit();
                System.exit(0);
            });
        }
    }

    @FXML
    private void showInfo()
    {
        PaneLoader.showAboutDevelopersDialog(stage);
    }
}
