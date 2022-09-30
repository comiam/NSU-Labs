package comiam.jcalc.calculator;

import comiam.jcalc.exception.parsing.ParsingLineException;
import comiam.jcalc.exception.parsing.ParsingValueException;
import comiam.jcalc.factory.OperationCreator;
import comiam.jcalc.log.Log;
import comiam.jcalc.utils.ArgChecker;

import java.util.ArrayList;
import java.util.Scanner;

public class InteractiveCalculator
{
    public static void execute()
    {
        System.out.println("JCalculator:");
        Scanner scan = new Scanner(System.in);

        String line = "";
        String opName = "";
        ArrayList<Object> args = new ArrayList<>();

        boolean opFilled = false;
        System.out.print("> ");
        while(scan.hasNext())
        {
            try
            {
                line = scan.nextLine().trim();
                if(line.isEmpty())
                    continue;

                Log.info("Call line: " + line);

                if(line.toLowerCase().equals("exit"))
                    break;

                Log.info("Stack before:");
                CalculatorStack.printStack();

                for(var word : line.split(" "))
                {
                    if(word.charAt(0) == '#')
                        break;

                    if(ArgChecker.containsSpecialSymbols(word))
                        throw new ParsingLineException("Wrong symbols!!!");

                    if(!opFilled)
                    {
                        opName = word;
                        opFilled = true;
                    } else
                    {
                        if(ArgChecker.isNumeric(word))
                        {
                            if(Double.isNaN(Double.parseDouble(word)))
                                throw new ParsingValueException("NaN value!!!");
                            else if(Double.isInfinite(Double.parseDouble(word)))
                                throw new ParsingValueException("Infinite value!!!");

                            args.add(Double.parseDouble(word));
                        } else
                            args.add(word);
                    }
                }
                if(!opName.isEmpty())
                    OperationCreator.getOperation(opName).setArgs(args.toArray(new Object[0])).exec();
                Log.info("Stack after:");
                CalculatorStack.printStack();
            } catch(Throwable e)
            {
                Log.error("Error in command: " + line + "\n\t\t\t" + e.getClass().getSimpleName()    + (e.getMessage() == null ? "" : ": " + e.getMessage()));
                System.err.println("Error in command: " + line + "\n" + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
            } finally
            {
                opFilled = false;
                args.clear();
                opName = "";
            }
            System.out.print("> ");
        }
        scan.close();

        if(CalculatorStack.getVarCount() > 1)
            System.out.println("In stack now " + CalculatorStack.getVarCount() + " variables.");
        else if(CalculatorStack.getVarCount() == 0)
            System.out.println("Stack is empty!!!");
    }
}
