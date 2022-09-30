package comiam.chat.client.connection;

import com.google.gson.reflect.TypeToken;
import comiam.chat.client.connection.message.*;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.data.units.Message;
import comiam.chat.client.gui.PaneLoader;
import comiam.chat.client.gui.fxml.MainMenuController;
import comiam.chat.client.json.JSONCore;
import comiam.chat.client.security.AESCipher;
import comiam.chat.client.security.DiffieHellmanKeyGen;
import comiam.chat.client.security.KeyDB;
import comiam.chat.client.time.Timer;
import comiam.chat.client.utils.Pair;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.math.BigInteger;
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
    private static boolean isConnected = true;
    private static ErrorType lastError = null;

    public static ErrorType getLastError()
    {
        return lastError;
    }

    public static boolean connectToServer(Stage stage, boolean showError)
    {
        if(!checkConnection().getFirst())
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
                if(showError)
                    showDefaultAlert(stage, "Oops", "Can't connect to server!", Alert.AlertType.ERROR);
                return false;
            }

            setCurrentConnection(socket);
        }

        return true;
    }

    public static String doRequest(Stage stage, RequestType type, boolean showError, String... args)
    {
        if(!connectToServer(stage, type != RequestType.DISCONNECT_MESSAGE))
            return null;

        if(!checkConnection().getSecond() && type != RequestType.SIGN_IN_MESSAGE && type != RequestType.SIGN_UP_MESSAGE && type != RequestType.DIFFIE_HELLMAN_INIT &&
                !authorize(stage, true, true, showError, LocalData.getUsername(), LocalData.getPassword()))
                    return null;

        Request request;
        BigInteger prime = null, generator, secretA = null, publicA;
        switch(type)
        {
            case DIFFIE_HELLMAN_INIT:
                prime = DiffieHellmanKeyGen.getPrime();
                generator = DiffieHellmanKeyGen.getGenerator(prime);
                secretA = DiffieHellmanKeyGen.getSecretValue();
                publicA = DiffieHellmanKeyGen.getPublicValue(generator, prime, secretA);

                request = new Request(type, args[0], generator.toString() + ";" + prime.toString() + ";" + publicA.toString(), LocalData.getCurrentSessionID());
                break;
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
                String encryptedMsg;
                try
                {
                    encryptedMsg = AESCipher.encryptMsg(args[1], KeyDB.getKey());
                }catch (Throwable e){e.printStackTrace();return null;}
                request = new Request(type, args[0], encryptedMsg, LocalData.getCurrentSessionID());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        if(!sendToServer(createPackage(saveToJSON(request))) && type != RequestType.DISCONNECT_MESSAGE)
        {
            clearData(false);
            showDefaultAlert(stage, "Oops", "Can't send message to server!", Alert.AlertType.ERROR);
            return null;
        }

        MessagePackage msgPkg = receiveFromServer();
        if(msgPkg == null && type != RequestType.DISCONNECT_MESSAGE)
        {
            clearData(false);
            showDefaultAlert(stage, "Oops", "Can't receive message from server!", Alert.AlertType.ERROR);
            return null;
        }

        assert msgPkg != null;
        System.out.println(msgPkg.getType());
        if(msgPkg.getType() == MessageType.SUCCESS_ANSWER)
        {
            if(type == RequestType.DIFFIE_HELLMAN_INIT)
            {
                BigInteger publicB = new BigInteger(msgPkg.getData());
                BigInteger key = DiffieHellmanKeyGen.getGeneratedKey(publicB, prime, secretA);
                KeyDB.setKey(key);
            }else if(type != RequestType.SIGN_IN_MESSAGE && type != RequestType.SIGN_UP_MESSAGE)
            {
                try
                {
                    return AESCipher.decryptMsg(msgPkg.getData(), KeyDB.getKey());
                }catch (Throwable e) {
                    e.printStackTrace();
                    return msgPkg.getData();
                }
            }
            return msgPkg.getData();
        }
        else
        {
            ErrorType errType;
            try
            {
                errType = JSONCore.parseFromJSON(AESCipher.decryptMsg(msgPkg.getData(), KeyDB.getKey()), ErrorType.class);
            }catch (Throwable e) {
                e.printStackTrace();
                return null;
            }

            String errString = "null";

            if(errType != null)
            {
                errString = errType.getMessageByType();
                lastError = errType;
            }

            if(showError)
                showDefaultAlert(stage, "Oops", errString, Alert.AlertType.ERROR);

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

    public static boolean authorize(Stage stage, boolean signIn, boolean secondTime, boolean showError, String username, String password)
    {
        String data;
        if((data = doRequest(stage, signIn ? RequestType.SIGN_IN_MESSAGE : RequestType.SIGN_UP_MESSAGE, showError, username, password)) == null)
            return false;

        LocalData.setCurrentSessionID(data);

        if(doRequest(stage, RequestType.DIFFIE_HELLMAN_INIT, showError, username, password) == null)
            return false;

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
                    String msg = null;
                    try
                    {
                        msg = AESCipher.decryptMsg(msgPkg.getData(), KeyDB.getKey());
                    }catch (Throwable e) {
                        e.printStackTrace();
                    }

                    switch(msgPkg.getType())
                    {
                        case MESSAGE_UPDATE:
                            Pair<String, ArrayList<Message>> messagePack = parseFromJSON(msg, new TypeToken<Pair<String, ArrayList<Message>>>(){}.getType());
                            assert messagePack != null;

                            if(messagePack.getFirst().equals(controller.getOpenedChatName()))
                            {
                                controller.appendNewMessages(messagePack.getSecond());
                                if(controller.isUserListIsOpened())
                                    controller.updateUsers();
                            }
                            break;
                        case CHAT_UPDATE:
                            ArrayList<Pair<String, Integer>> list = parseFromJSON(msg, new TypeToken<ArrayList<Pair<String, Integer>>>(){}.getType());
                            controller.loadChatList(list);
                            break;
                        default:
                            throw new RuntimeException("What the fuck...");
                    }
                }

                return true;
            }, Timer.SECOND / 10);

            Timer.subscribeEvent(() -> {
                var res = checkConnection();

                if(isConnected && !res.getFirst())
                    isConnected = false;
                else if(!isConnected && res.getFirst())
                {
                    if(controller.isUserListIsOpened())
                        controller.reloadUserList();
                    else if(controller.getOpenedChatName() != null)
                        controller.reloadMessageList();
                    else
                        controller.reloadChatList();

                    isConnected = true;
                }else if(!isConnected && !res.getFirst())
                    connectToServer(stage, false);

                return true;
            }, Timer.SECOND / 2);

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

    public static ArrayList<Pair<String, String>> getUserFromChat(Stage stage, String name)
    {
        String data;
        if((data = doRequest(stage, RequestType.GET_USERS_OF_CHAT_MESSAGE, false, name)) == null)
            return null;

        ArrayList<Pair<String, String>> chatList = parseFromJSON(data, new TypeToken<ArrayList<Pair<String, String>>>(){}.getType());

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
    }

    public static boolean createChat(Stage stage, String name)
    {
        return doRequest(stage, RequestType.CREATE_CHAT_MESSAGE, true, name) != null;
    }

    public static void clearData(boolean clearConnectionData)
    {
        if(clearConnectionData)
        {
            isConnected = true;
            currentIP = null;
            currentPort = -1;
        }
        closeConnection();
    }

    /**
     * @return a pair of booleans - connected state and authorized state
     */
    public static Pair<Boolean, Boolean> checkConnection()
    {
        Request request = new Request(RequestType.CHECK_CONNECTED_MESSAGE, LocalData.getCurrentSessionID());

        if(!sendToServer(createPackage(saveToJSON(request))))
            return new Pair<>(false, false);

        MessagePackage msgPkg = receiveFromServer();
        if(msgPkg == null)
            return new Pair<>(false, false);

        return new Pair<>(true, msgPkg.getData().equals("true"));
    }
}
