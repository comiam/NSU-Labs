package comiam.chat.client.connection;

import comiam.chat.client.connection.message.MessagePackage;
import comiam.chat.client.connection.message.MessageType;
import comiam.chat.client.connection.message.Request;
import comiam.chat.client.connection.message.RequestType;
import comiam.chat.client.data.LocalData;
import comiam.chat.client.utils.Pair;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.net.Socket;

import static comiam.chat.client.connection.Connection.receiveFromServer;
import static comiam.chat.client.connection.Connection.sendToServer;
import static comiam.chat.client.gui.Dialogs.showDefaultAlert;
import static comiam.chat.client.gui.Dialogs.showInputDialog;
import static comiam.chat.client.json.JSONCore.saveToJSON;
import static comiam.chat.client.utils.ByteUtils.createPackage;
import static comiam.chat.client.utils.IPUtils.getDataIP;

public class ClientServer
{
    private static Socket currentConnection;
    private static String currentIP;
    private static int currentPort;

    public static boolean connectToServer(Stage stage)
    {
        if(currentConnection == null || !currentConnection.isConnected())
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

            currentConnection = socket;
        }

        return true;
    }

    public static boolean authorize(Stage stage, boolean signIn, String username, String password)
    {
        Request request = new Request(signIn ? RequestType.SIGN_IN_MESSAGE : RequestType.SIGN_UP_MESSAGE, username, password);

        if(!sendToServer(currentConnection, createPackage(saveToJSON(request))))
        {
            closeConnection();
            showDefaultAlert(stage, "Oops", "Can't send message to server!", Alert.AlertType.ERROR);
            return false;
        }

        MessagePackage msgPkg = receiveFromServer(currentConnection);
        if(msgPkg == null)
        {
            closeConnection();
            showDefaultAlert(stage, "Oops", "Can't receive message from server!", Alert.AlertType.ERROR);
            return false;
        }

        if(msgPkg.getType() == MessageType.SUCCESS_ANSWER)
        {
            LocalData.setCurrentSessionID(msgPkg.getData());
            stage.close();
            return true;
        }else
        {
            showDefaultAlert(stage, "Oops", msgPkg.getData(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private static void closeConnection()
    {
        try
        {
            currentIP = null;
            currentPort = -1;
            currentConnection.close();
        }catch(Throwable ignored){}
    }
}
