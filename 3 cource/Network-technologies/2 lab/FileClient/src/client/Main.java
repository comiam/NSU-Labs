package client;

import java.io.File;
import java.io.FileNotFoundException;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("invalid arg size!");
            return;
        }

        int port;

        try
        {
            port = Integer.parseInt(args[2]);
            File file = new File(args[0]);
            if(!file.exists() || !file.canRead())
                throw new FileNotFoundException();
        }catch (Throwable e){
            System.out.println("invalid args!");
            e.printStackTrace();

            return;
        }

        try
        {
            ClientSide.sendFile(args[1], port, args[0]);
        } catch (Throwable e)
        {
            System.out.println("unexpected error during sending a file!");
            e.printStackTrace();
        }
    }
}
