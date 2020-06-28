package comiam.chat.server.main;

import comiam.chat.server.core.ServerCore;
import comiam.chat.server.logger.Log;
import comiam.chat.server.xml.XMLCore;

import java.util.Scanner;

import static comiam.chat.server.main.ConsoleCommands.SHUTDOWN_SERVER;
import static comiam.chat.server.main.ConsoleCommands.isValidCommand;

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

        Scanner scanner = new Scanner(System.in);
        String line;

        console: do
        {
            printNewLine();
            line = scanner.nextLine().trim();

            if(line.split(" ").length == 0)
                continue;
            if(!isValidCommand(line))
            {
                println("Unknown command: " + line);
                continue;
            }

            switch(line)
            {
                case SHUTDOWN_SERVER:
                    ServerCore.shutdown(false);
                    break console;
                    /* etc */
                default:
                    println("Unknown command: " + line);
            }
        }while(true);
    }

    private static void printNewLine()
    {
        System.out.println("> ");
        System.out.flush();
    }

    private static void println(String str)
    {
        System.out.println(str + "\n");
        System.out.flush();
    }
}
