package comiam.chat.client.gui.fxml;

import comiam.chat.client.connection.ClientServer;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.PaneLoader;
import comiam.chat.client.time.Date;
import comiam.chat.client.utils.Pair;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Popup;
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

    private VBox userList;

    private Stage stage;
    private TextFlow messageArea;
    private String openedChatName = null;
    private ArrayList<Message> currentMessages;

    private boolean loadedSuccessfully = false;
    private boolean userListIsOpened = false;

    public boolean isUserListIsOpened()
    {
        return userListIsOpened;
    }

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

            Platform.runLater(() -> addMessage(messageArea, newPack.get(finalI).getUsername(), newPack.get(finalI).getDate(), newPack.get(finalI).getText()));
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
            {
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

    private void showUserList()
    {
        var userArr = ClientServer.getUserFromChat(stage, openedChatName);

        if(userArr == null)
        {
            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            userArr = ClientServer.getUserFromChat(stage, openedChatName);
            if(userArr == null)
            {
                showDefaultAlert(stage, "Oops", "Can't get user list, sorry...", Alert.AlertType.ERROR);
                return;
            }
        }
        userListIsOpened = true;

        updateButton.setVisible(false);
        addChatButton.setOnAction((e) -> exitFromUserList());

        userList = PaneLoader.getUserListPane();

        assert userList != null;
        TextFlow userFlow = getTextFlow();
        userFlow.getChildren().addListener(
                (ListChangeListener<Node>) ((change) -> {
                    userFlow.layout();
                    ((ScrollPane) userList.getChildren().get(1)).layout();
                    ((ScrollPane) userList.getChildren().get(1)).setVvalue(1.0f);
                }));

        ((ScrollPane) userList.getChildren().get(1)).setContent(userFlow);

        fillUserList(userArr);

        mainPanel.getChildren().remove(msgBox);
        mainPanel.getChildren().add(userList);
    }

    public void updateUsers()
    {
        var userArr = ClientServer.getUserFromChat(stage, openedChatName);

        if(userArr == null)
        {
            var pair = ClientServer.checkConnection();

            if(!pair.getFirst())
                return;

            userArr = ClientServer.getUserFromChat(stage, openedChatName);
            if(userArr == null)
            {
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

        TextFlow msgArea = getTextFlow();

        msgArea.getChildren().addListener(
                (ListChangeListener<Node>) ((change) -> {
                    msgArea.layout();
                    ((ScrollPane) msgBox.getChildren().get(0)).layout();
                    ((ScrollPane) msgBox.getChildren().get(0)).setVvalue(1.0f);
                }));

        ((ScrollPane) msgBox.getChildren().get(0)).setContent(msgArea);

        TextField field = (TextField) msgBox.getChildren().get(1);

        for(var msg : messages)
            addMessage(msgArea, msg.getUsername(), msg.getDate(), msg.getText());

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
                    addMessage(msgArea, LocalData.getUsername(), date, field.getText().trim());
                    currentMessages.add(new Message(field.getText().trim(), date, LocalData.getUsername()));
                    field.setText("");
                }
            }
        });
        mainPanel.getChildren().remove(chatPanel);
        mainPanel.getChildren().add(msgBox);

        messageArea = msgArea;
        openedChatName = nameChat;
        currentMessages = messages;
    }

    private void addUserToList(TextFlow userFlow, Pair<String, String> usr)
    {
        Text username0 = new Text("User: ");
        username0.setFont(Font.font(username0.getFont().getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, 13));

        Text username1 = new Text(usr.getFirst() + "\n");
        username1.setFont(Font.font(username1.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 13));

        Text lastActive = new Text("Last active: " + usr.getSecond() + "\n");
        lastActive.setFont(Font.font(lastActive.getFont().getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, 12));
        lastActive.setUnderline(true);

        userFlow.getChildren().addAll(username0, username1, lastActive);
    }

    private void addMessage(TextFlow msgArea, String username, String date, String message)
    {
        if(username.equals("server"))
        {
            Label messageT = new Label(message);
            messageT.setFont(Font.font(messageT.getFont().getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, 14));
            messageT.setTextFill(Color.RED);
            messageT.setMinWidth(msgArea.getPrefWidth());
            messageT.setAlignment(Pos.TOP_CENTER);
            setPopup(messageT, date);

            msgArea.getChildren().add(messageT);
        }else
        {
            Text usernameT = new Text((msgArea.getChildren().isEmpty() ? "" : "\n") + username + "\n");
            usernameT.setFont(Font.font(usernameT.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, 13));
            msgArea.getChildren().add(usernameT);

            Text messageT = new Text(message);
            messageT.setFont(Font.font(messageT.getFont().getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, 15));
            setPopup(messageT, date);

            msgArea.getChildren().add(messageT);
        }
    }

    private TextFlow getTextFlow()
    {
        TextFlow msgArea = new TextFlow();
        msgArea.setPrefHeight(306);
        msgArea.setPrefWidth(279);
        msgArea.setMaxWidth(Double.MAX_VALUE);
        msgArea.setMaxHeight(Region.USE_COMPUTED_SIZE);
        msgArea.setMinHeight(Region.USE_COMPUTED_SIZE);
        msgArea.setMinWidth(Region.USE_COMPUTED_SIZE);

        return msgArea;
    }

    private void setPopup(Node text, String popupMsg)
    {
        Label label = new Label(popupMsg);
        label.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        Popup popup = new Popup();
        popup.getContent().add(label);

        double offset = 5;
        text.setOnMouseMoved((e) -> {
            popup.setAnchorX(e.getScreenX() + offset * 2);
            popup.setAnchorY(e.getScreenY() + offset);
        });
        text.setOnMouseEntered(e -> popup.show(text, e.getScreenX(), e.getScreenY() + offset));
        text.setOnMouseExited(e -> popup.hide());
    }

    private void exitFromChat()
    {
        updateButton.setText("upd");
        updateButton.setOnAction((e) -> loadChatList(false));

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

            ArrayList<Message> messages = ClientServer.getMessagesFromChat(stage, name);
            if(messages == null)
            {
                var pair = ClientServer.checkConnection();

                if(!pair.getFirst())
                    return;

                boolean connected = ClientServer.connectToChat(stage, name);
                if(!connected)
                {
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
        setPopup(nameL, name);

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
            name = showInputDialog(stage, "Dialog", "lol", "Please input chat name:");
            if(name == null)
                return;

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
        PaneLoader.showEnterDialog();
    }

    @FXML
    private void showInfo()
    {
        PaneLoader.showAboutDevelopersDialog(stage);
    }
}
