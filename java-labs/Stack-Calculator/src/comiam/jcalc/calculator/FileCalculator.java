package comiam.jcalc.calculator;

import comiam.jcalc.exception.parsing.ParsingLineException;
import comiam.jcalc.exception.parsing.ParsingValueException;
import comiam.jcalc.factory.OperationCreator;
import comiam.jcalc.log.Log;
import comiam.jcalc.utils.ArgChecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileCalculator
{
    public static void execute(String file)
    {
        Scanner scan;
        try
        {
            scan = new Scanner(new File(file));
        } catch(IOException e)
        {
            e.printStackTrace(System.err);
            return;
        }

        String line;
        String opName = "";
        ArrayList<Object> args = new ArrayList<>();

        boolean opFilled = false;
        long lineCounter = 1;

        while(scan.hasNext())
        {
            try
            {
                line = scan.nextLine().trim();
                if(line.isEmpty())
                    continue;

                Log.info("Call line: " + line);
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
                Log.error("Error on line " + lineCounter + ":\n\t\t\t" + e.getClass().getSimpleName()    + (e.getMessage() == null ? "" : ": " + e.getMessage()));
                System.err.println("Error on line " + lineCounter + ":\n" + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
            } finally
            {
                opFilled = false;
                args.clear();
                opName = "";
                lineCounter++;
            }
        }
        scan.close();

        if(CalculatorStack.getVarCount() > 1)
            System.out.println("In stack now " + CalculatorStack.getVarCount() + " variables.");
        else if(CalculatorStack.getVarCount() == 0)
            System.out.println("Stack is empty!!!");
    }
}
