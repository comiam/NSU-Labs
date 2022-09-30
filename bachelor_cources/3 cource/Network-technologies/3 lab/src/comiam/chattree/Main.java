package comiam.chattree;

import comiam.chattree.node.ChatNode;

public class Main
{
    public static void main(String[] args)
    {
        Integer port, percentOfLost, neighbourPort;

        switch (args.length)
        {
            case 3:
                if ((percentOfLost = getInt(args[1])) == null || (port = getInt(args[2])) == null)
                {
                    System.out.println("invalid args!");
                    return;
                }

                ChatNode.start(args[0], percentOfLost, port, null, null);
                return;
            case 5:
                if ((percentOfLost = getInt(args[1])) == null || (port = getInt(args[2])) == null || (neighbourPort = getInt(args[4])) == null)
                {
                    System.out.println("invalid args!");
                    return;
                }

                ChatNode.start(args[0], percentOfLost, port, args[3], neighbourPort);
                return;
            default:
                System.out.println("invalid arg size!");
        }
    }

    private static Integer getInt(String val)
    {
        try
        {
            return Integer.parseInt(val);
        } catch (Throwable e)
        {
            return null;
        }
    }
}
