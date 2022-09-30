package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;

import static server.DataBundle.*;

public class ClientHandler implements Runnable
{
    private Socket clientDialog;
    private final byte[] buffer;
    private final int id;

    private long fileSizeBytes = 0;

    ClientHandler(Socket client, int id)
    {
        this.id = id;
        clientDialog = client;
        buffer = new byte[4 * 1024];
    }

    private String getTotalData()
    {
        return id + " client state: " + getTotalBytesReceived(id) + "/" + fileSizeBytes + " bytes received";
    }

    @Override
    public void run()
    {
        FileOutputStream fos = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        SpeedHandler speedChecker = new SpeedHandler(id);
        Thread speedCheckThread = null;

        try
        {
            if(!checkDirectory())
                throw new FileSystemException("uploads/");

            dis = new DataInputStream(clientDialog.getInputStream());
            dos = new DataOutputStream(clientDialog.getOutputStream());

            String fileName = dis.readUTF();
            fileSizeBytes = dis.readLong();

            System.out.println("File receiving in handler " + id + ": " + fileName);

            while (new File("uploads/" + fileName).exists())
                fileName = "(new)".concat(fileName);

            fos = new FileOutputStream("uploads/" + fileName);

            addNewCounter(id);

            speedCheckThread = new Thread(speedChecker);
            speedCheckThread.start();

            int bytesRead;
            while (getTotalBytesReceived(id) < fileSizeBytes && (bytesRead = dis.read(buffer)) != -1)
            {
                addToTotal(id, bytesRead);
                fos.write(buffer, 0, bytesRead);
            }
            speedChecker.finish();
            speedCheckThread.join();



            System.out.println(getTotalData());
            if (getTotalBytesReceived(id) == fileSizeBytes)
                dos.writeUTF("successful transfer " + getTotalData());
             else
                dos.writeUTF("Error: " + getTotalData());

            removeCounter(id);

             dis.close();
             fos.close();
             dos.close();
             clientDialog.close();

             dis = null;
             fos = null;
             dos = null;
             clientDialog = null;
             speedCheckThread = null;
        } catch (Exception e)
        {
            System.out.println("Unexpected error during receiving file in handler " + id);
            speedChecker.finish();
            e.printStackTrace();

            try {if (fos != null) fos.close();} catch (Throwable ignored) {}
            try {if (dos != null) dos.close();} catch (Throwable ignored) {}
            try {if (dis != null) dis.close();} catch (Throwable ignored) {}
            try {if (clientDialog != null) clientDialog.close();} catch (Throwable ignored) {}
            try
            {
                if(speedCheckThread != null)
                {
                    speedChecker.finish();
                    speedCheckThread.join();
                }
            } catch (Throwable ignored) {}
        }

        System.out.println("Handler " + id + " closed connection...");
    }

    private boolean checkDirectory()
    {
        Path path = Paths.get("uploads");
        if (!Files.exists(path))
            return new File(path.toString()).mkdir();

        return true;
    }
}
