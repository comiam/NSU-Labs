package comiam.jcalc.main;

import comiam.jcalc.calculator.FileCalculator;
import comiam.jcalc.calculator.InteractiveCalculator;

public class Main
{
    public static void main(String[] args)
    {
        if(args.length > 0)
            FileCalculator.execute(args[0]);
        else
            InteractiveCalculator.execute();
    }
}
