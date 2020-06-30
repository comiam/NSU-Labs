package comiam.chat.server.core;

import comiam.chat.server.connection.ConnectionTimers;
import comiam.chat.server.data.ServerData;
import comiam.chat.server.logger.Log;
import comiam.chat.server.threads.InputHandler;
import comiam.chat.server.threads.MessageHandler;

public class ServerCore
{
    private static String dataBasePath;
    private static Thread inputThread;
    private static Thread messageThread;
    public static  boolean running = false;

    public static void start(int port, String dataBasePath)
    {
        if(running)
            return;

        ServerCore.dataBasePath = dataBasePath;

        Log.info("Starting server threads...");

        inputThread = new Thread(new InputHandler(port));
        messageThread = new Thread(new MessageHandler());

        if(!ServerData.loadData(ServerCore.dataBasePath))
            return;

        ConnectionTimers.setTimerHandler();
        inputThread.start();
        messageThread.start();

        String message = "Server started on port " + port + " with " +
                (dataBasePath.equals("null") ? "empty database" : "database in " + dataBasePath) + ".";

        System.out.println(message);
        Log.info(message);

        running = true;
    }

    public static void shutdown(boolean onError)
    {
        if(!running)
            return;

        if(!onError)
            ServerData.saveData(ServerCore.dataBasePath);

        inputThread.interrupt();
        messageThread.interrupt();

        running = false;
    }
}
