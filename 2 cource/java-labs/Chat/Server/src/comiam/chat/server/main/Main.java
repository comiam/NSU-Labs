package comiam.chat.server.main;

import comiam.chat.server.core.ServerCore;
import comiam.chat.server.logger.Log;

import java.util.Scanner;

import static comiam.chat.server.json.JSONCore.parseFromFile;
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

        Configuration config = parseFromFile(args[0], Configuration.class);

        if(config == null)
        {
            System.out.println("Error on loading configuration.");
            return;
        }

        if(config.isLogging())
        {
            Log.init();
            Log.enableInfoLogging();
            Log.enableErrorLogging();
        }

        if(!ServerCore.start(config.getPort(), config.getDbPath()))
            return;

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
                    ServerCore.shutdown();
                    break console;
                    /* etc */
                default:
                    println("Unknown command: " + line);
            }
        }while(true);
    }

    private static void printNewLine()
    {
        System.out.print("> ");
        System.out.flush();
    }

    private static void println(String str)
    {
        System.out.println(str + "\n");
        System.out.flush();
    }
}
