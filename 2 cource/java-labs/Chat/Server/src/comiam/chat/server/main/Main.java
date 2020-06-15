package comiam.chat.server.main;

import comiam.chat.server.core.ServerCore;
import comiam.chat.server.logger.Log;
import comiam.chat.server.xml.XMLCore;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        if(args.length != 1)
        {
            System.out.println("Invalid arg size!");
            return;
        }

        var res = XMLCore.loadConfig(args[0]);

        if(res == null)
        {
            System.out.println("Error on loading configuration: " + XMLCore.getParserError());
            return;
        }

        if(res.getFirst().getSecond())
        {
            Log.init();
            Log.enableInfoLogging();
            Log.enableErrorLogging();
        }

        ServerCore.start(res.getFirst().getFirst(), res.getSecond());

        //FIXME release terminal

    }
}
