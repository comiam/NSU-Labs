package comiam.chat.client.connection;

import com.google.gson.reflect.TypeToken;
import comiam.chat.client.connection.message.MessagePackage;
import comiam.chat.client.connection.message.MessageType;
import comiam.chat.client.connection.message.Request;
import comiam.chat.client.connection.message.RequestType;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.MainMenu;
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

    public static String doRequest(Stage stage, RequestType type, String... args)
    {
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
            case GET_ONLINE_USERS_OF_CHAT_MESSAGE:
            case GET_MESSAGES_FROM_CHAT_MESSAGE:
            case CREATE_CHAT_MESSAGE:
            case CONNECT_TO_CHAT_MESSAGE:
                request = new Request(type, args[0], null, LocalData.getCurrentSessionID());
                break;
            case SEND_MESSAGE_MESSAGE:
                request = new Request(type, args[0], args[1], LocalData.getCurrentSessionID());
                break;
        }

        if(!sendToServer(createPackage(saveToJSON(request))))
        {
            clearData();
            showDefaultAlert(stage, "Oops", "Can't send message to server!", Alert.AlertType.ERROR);
            return null;
        }

        MessagePackage msgPkg = receiveFromServer();
        if(msgPkg == null)
        {
            clearData();
            showDefaultAlert(stage, "Oops", "Can't receive message from server!", Alert.AlertType.ERROR);
            return null;
        }

        if(msgPkg.getType() == MessageType.SUCCESS_ANSWER)
            return msgPkg.getData();
        else
        {
            showDefaultAlert(stage, "Oops", msgPkg.getData(), Alert.AlertType.ERROR);
            return null;
        }
    }

    public static boolean authorize(Stage stage, boolean signIn, String username, String password)
    {
        String data;
        if((data = doRequest(stage, signIn ? RequestType.SIGN_IN_MESSAGE : RequestType.SIGN_UP_MESSAGE, username, password)) == null)
            return false;

        LocalData.setCurrentSessionID(data);
        stage.close();

        MainMenu.show(username);
        return true;
    }

    public static ArrayList<Pair<String, Integer>> getChatLists(Stage stage)
    {
        String data;
        if((data = doRequest(stage, RequestType.GET_CHATS_MESSAGE)) == null)
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
        if((data = doRequest(stage, RequestType.GET_USERS_OF_CHAT_MESSAGE, name)) == null)
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
        if((data = doRequest(stage, RequestType.GET_MESSAGES_FROM_CHAT_MESSAGE, name)) == null)
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
        doRequest(null, RequestType.DISCONNECT_MESSAGE);
    }

    public static boolean createChat(Stage stage, String name)
    {
        return doRequest(stage, RequestType.CREATE_CHAT_MESSAGE, name) != null;
    }

    public static void clearData()
    {
        currentIP = null;
        currentPort = -1;
        closeConnection();
    }
}
