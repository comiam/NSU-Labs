package server;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("invalid arg size!");
            return;
        }

        int port;

        try
        {
            port = Integer.parseInt(args[0]);
        }catch (Throwable e){
            System.out.println("invalid port!");
            e.printStackTrace();

            return;
        }

        try
        {
            Server.init(port);
        }catch(Throwable e) {
            System.out.println("Unexpected problem!");
            e.printStackTrace();
        }

    }
}
