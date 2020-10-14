package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

class Server
{
    static void init(int port) throws IOException
    {
        int processors = Runtime.getRuntime().availableProcessors();
        var executeIt = Executors.newFixedThreadPool(processors);

        int nextId = 1;

        ServerSocket server = new ServerSocket(port, processors);
        System.out.println("Server started on port " + port);
        System.out.println("IP of server: " + Inet.getCurrentIPv4());

        while (!server.isClosed())
        {
            var sock = server.accept();
            System.out.println("------------------------------------------------------");
            System.out.println("New connection by " + sock.getInetAddress());
            executeIt.execute(new ClientHandler(sock, nextId++));
            System.out.println("Connection accepted!");
        }
        executeIt.shutdown();
        DataBundle.clearAll();
    }
}
