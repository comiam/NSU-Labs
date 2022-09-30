package comiam.sapper.util;

public class IOUtils
{
    public static <T> void print(T message)
    {
        if(System.console() != null)
        {
            System.console().printf("%s", message.toString());
            System.console().flush();
        }else
        {
            System.out.printf("%s", message.toString());
            System.out.flush();
        }
    }

    public static <T> void println(T message)
    {
        if(System.console() != null)
        {
            System.console().printf("%s\n", message.toString());
            System.console().flush();
        }else
        {
            System.out.printf("%s\n", message.toString());
            System.out.flush();
        }
    }

    public static void println()
    {
        println("");
    }
}
