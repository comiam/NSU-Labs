package comiam.sapper.main;

import comiam.sapper.game.Minesweeper;

public class Main
{
    public static void main(String[] args)
    {
        boolean two, mainGUI;
        switch(args.length)
        {
            case 0 -> {
                two = false;
                mainGUI = true;
            }
            case 1 -> {
                two = false;
                switch(args[0].toLowerCase())
                {
                    case "gui" -> mainGUI = true;
                    case "text" -> mainGUI = false;
                    default -> {
                        System.out.println("Unknown arg: " + args[0] + "!!!");
                        return;
                    }
                }
            }
            case 2 -> {
                if(!args[0].toLowerCase().equals("enall"))
                {
                    System.out.println("Unknown arg: " + args[0] + "!!!");
                    return;
                }
                two = true;

                switch(args[1])
                {
                    case "m-gui" -> mainGUI = true;
                    case "m-text" -> mainGUI = false;
                    default -> {
                        System.out.println("Unknown arg: " + args[1] + "!!!");
                        return;
                    }
                }
            }
            default -> {
                System.out.println("Too many args!!!");
                return;
            }
        }
        Minesweeper.showMenu(two, mainGUI);
    }
}
