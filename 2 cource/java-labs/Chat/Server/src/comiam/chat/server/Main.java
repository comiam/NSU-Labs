package comiam.chat.server;

import comiam.chat.server.core.ServerCore;
import comiam.chat.server.logger.Log;

import static comiam.chat.server.utils.ArgChecker.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if(args.length != 2)
        {
            System.out.println("Invalid arg size!");
            return;
        }

        if(isNotNumeric(args[0]))
        {
            System.out.println("Invalid port value!");
            return;
        }

        if(!isBoolean(args[1]))
        {
            System.out.println("Invalid logging flag value!");
            return;
        }

        if(Boolean.parseBoolean(args[1]))
        {
            Log.init();
            Log.enableInfoLogging();
            Log.enableErrorLogging();
        }

        ServerCore.start(Integer.parseInt(args[0]));

        //FIXME release terminal

    }
}
