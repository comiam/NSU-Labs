package comiam.chat.client.connection;

import com.google.gson.reflect.TypeToken;
import comiam.chat.client.connection.message.MessagePackage;
import comiam.chat.client.connection.message.MessageType;
import comiam.chat.client.connection.message.Request;
import comiam.chat.client.connection.message.RequestType;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.PaneLoader;
import comiam.chat.client.gui.fxml.MainMenuController;
import comiam.chat.client.time.Timer;
import comiam.chat.client.utils.Pair;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static comiam.chat.client.connection.Connection.*;
import static comiam.chat.client.gui.Dialogs.showDefaultAlert;
import static comiam.chat.client.gui.Dialogs.showInputDialog;
import static comiam.chat.client.json.JSONCore.parseFromJSON;
import static comiam.chat.client.json.JSONCore.saveToJSON;
import static comiam.chat.client.utils.ByteUtils.createPackage;
import static comiam.chat.client.utils.IPUtils.getDataIP;

public class ClientServer
{
    private static String currentIP;
    private static int currentPort;

    public static boolean connectToServer(Stage stage)
    {
        if(!isCurrentConnectionAvailable())
        {
            if(currentIP == null)
            {
                String localIP = null;

                while(localIP == null)
                {
                    localIP = showInputDialog(stage, "Dialog", "00.00.00.00:0000", "Please input ip address of server with port:");
                    if(localIP == null)
                        return false;

                    Pair<String, Integer> pair;
                    if((pair = getDataIP(localIP)) == null)
                    {
                        showDefaultAlert(stage, "Oops", "You typed wrong IP!", Alert.AlertType.ERROR);
                        continue;
                    }

                    currentIP = pair.getFirst();
                    currentPort = pair.getSecond();
                }
            }

            Socket socket = new Socket();
            try
            {
                socket.connect(new InetSocketAddress(currentIP, currentPort), 5000);
            }catch(Throwable e)
            {
                showDefaultAlert(stage, "Oops", "Can't connect to server!", Alert.AlertType.ERROR);
                currentIP = null;
                currentPort = 0;
                return false;
            }

            setCurrentConnection(socket);
        }

        return true;
    }

    public static String doRequest(Stage stage, RequestType type, boolean showWarning, String... args)
    {
        connectToServer(stage);
        if(LocalData.isDisconnected() && type != RequestType.SIGN_IN_MESSAGE && type != RequestType.SIGN_UP_MESSAGE)
            authorize(stage, true, true, LocalData.getUsername(), LocalData.getPassword());

        Request request = null;

        switch(type)
        {
            case SIGN_IN_MESSAGE:
            case SIGN_UP_MESSAGE:
                request = new Request(type, args[0], args[1]);
                break;
            case GET_CHATS_MESSAGE:
            case DISCONNECT_MESSAGE:
                request = new Request(type, LocalData.getCurrentSessionID());
                break;
            case GET_USERS_OF_CHAT_MESSAGE:
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
            case CREATE_CHAT_MESSAGE:
            case CONNECT_TO_CHAT_MESSAGE:
                request = new Request(type, args[0], null, LocalData.getCurrentSessionID());
                break;
            case SEND_MESSAGE_MESSAGE:
                request = new Request(type, args[0], args[1], LocalData.getCurrentSessionID());
                break;
        }

        if(!sendToServer(createPackage(saveToJSON(request))) && type != RequestType.DISCONNECT_MESSAGE)
        {
            clearData(true);
            showDefaultAlert(stage, "Oops", "Can't send message to server!", Alert.AlertType.ERROR);
            return null;
        }

        MessagePackage msgPkg = receiveFromServer();
        if(msgPkg == null && type != RequestType.DISCONNECT_MESSAGE)
        {
            clearData(true);
            showDefaultAlert(stage, "Oops", "Can't receive message from server!", Alert.AlertType.ERROR);
            return null;
        }

        assert msgPkg != null;
        if(msgPkg.getType() == MessageType.SUCCESS_ANSWER)
            return msgPkg.getData();
        else
        {
            if(showWarning)
                showDefaultAlert(stage, "Oops", msgPkg.getData(), Alert.AlertType.ERROR);

            if(type == RequestType.SIGN_IN_MESSAGE || type == RequestType.SIGN_UP_MESSAGE)
                clearData(false);
            return null;
        }
    }

    public static boolean connectToChat(Stage stage, String chatName)
    {
        return doRequest(stage, RequestType.CONNECT_TO_CHAT_MESSAGE, true, chatName) != null;
    }

    public static boolean sendMessage(Stage stage, String chatName, String message)
    {
        return doRequest(stage, RequestType.SEND_MESSAGE_MESSAGE, true, chatName, message) != null;
    }

    public static boolean authorize(Stage stage, boolean signIn, boolean secondTime, String username, String password)
    {
        String data;
        if((data = doRequest(stage, signIn ? RequestType.SIGN_IN_MESSAGE : RequestType.SIGN_UP_MESSAGE, true, username, password)) == null)
            return false;

        LocalData.setCurrentSessionID(data);
        LocalData.setDisconnected(false);

        if(!secondTime)
        {
            stage.close();

            LocalData.setUsername(username);
            LocalData.setPassword(password);
            MainMenuController controller = PaneLoader.showMainMenu(username);

            Timer.subscribeEvent(() -> {
                if(Connection.haveMessageFromServer())
                {
                    MessagePackage msgPkg = receiveFromServer();

                    if(msgPkg == null)
                        return true;

                    switch(msgPkg.getType())
                    {
                        case MESSAGE_UPDATE:
                            Pair<String, ArrayList<Message>> messagePack = parseFromJSON(msgPkg.getData(), new TypeToken<Pair<String, ArrayList<Message>>>(){}.getType());
                            assert messagePack != null;
                            if(messagePack.getFirst().equals(controller.getOpenedChatName()))
                                controller.appendNewMessages(messagePack.getSecond());
                            break;
                        case CHAT_UPDATE:
                            ArrayList<Pair<String, Integer>> list = parseFromJSON(msgPkg.getData(), new TypeToken<ArrayList<Pair<String, Integer>>>(){}.getType());
                            controller.loadChatList(list);
                            break;
                        case DISCONNECT_NOTICE:
                            LocalData.setDisconnected(true);
                        default:
                            throw new RuntimeException("What the fuck...");
                    }
                }

                return true;
            }, Timer.SECOND / 10);
            Timer.start();
        }

        return true;
    }

    public static ArrayList<Pair<String, Integer>> getChatLists(Stage stage)
    {
        String data;
        if((data = doRequest(stage, RequestType.GET_CHATS_MESSAGE, false)) == null)
            return null;

        ArrayList<Pair<String, Integer>> chatList = parseFromJSON(data, new TypeToken<ArrayList<Pair<String, Integer>>>(){}.getType());

        if(chatList == null)
        {
            showDefaultAlert(stage, "Oops", "Cannot get chat list!", Alert.AlertType.ERROR);
            return null;
        }

        return chatList;
    }

    public static ArrayList<String> getUserFromChat(Stage stage, String name)
    {
        String data;
        if((data = doRequest(stage, RequestType.GET_USERS_OF_CHAT_MESSAGE, false, name)) == null)
            return null;

        ArrayList<String> chatList = parseFromJSON(data, new TypeToken<ArrayList<String>>(){}.getType());

        if(chatList == null)
        {
            showDefaultAlert(stage, "Oops", "Cannot get user list!", Alert.AlertType.ERROR);
            return null;
        }

        return chatList;
    }

    public static ArrayList<Message> getMessagesFromChat(Stage stage, String name)
    {
        String data;
        if((data = doRequest(stage, RequestType.GET_MESSAGES_FROM_CHAT_MESSAGE, false, name)) == null)
            return null;

        ArrayList<Message> chatList = parseFromJSON(data, new TypeToken<ArrayList<Message>>(){}.getType());

        if(chatList == null)
        {
            showDefaultAlert(stage, "Oops", "Cannot get message list!", Alert.AlertType.ERROR);
            return null;
        }

        return chatList;
    }

    public static void disconnect()
    {
        doRequest(null, RequestType.DISCONNECT_MESSAGE, false);
        LocalData.setDisconnected(true);
    }

    public static boolean createChat(Stage stage, String name)
    {
        return doRequest(stage, RequestType.CREATE_CHAT_MESSAGE, true, name) != null;
    }

    public static void clearData(boolean clearConnectionData)
    {
        if(clearConnectionData)
        {
            currentIP = null;
            currentPort = -1;
        }
        closeConnection();
    }
}
