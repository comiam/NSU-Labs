package comiam.chat.server.core;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.logger.Log;
import comiam.chat.server.threads.InputHandler;
import comiam.chat.server.threads.MessageHandler;
import comiam.chat.server.connection.ConnectionTimers;

public class ServerCore
{
    private static Thread inputThread;
    private static Thread messageThread;
    public static  boolean running = false;

    public static void start(int port)
    {
        if(running)
            return;

        Log.info("Starting server threads...");

        inputThread = new Thread(new InputHandler(port));
        messageThread = new Thread(new MessageHandler());

        ServerData.loadData();

        ConnectionTimers.setTimerHandler();
        inputThread.start();
        messageThread.start();

        running = true;
    }

    public static void shutdown(boolean onError)
    {
        if(!running)
            return;

        if(!onError)
            ServerData.saveData();

        inputThread.interrupt();
        messageThread.interrupt();

        running = false;
    }
}
