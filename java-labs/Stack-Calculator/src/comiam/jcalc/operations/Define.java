package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.operations.ExecuteOperationException;
import comiam.jcalc.exception.operations.InvalidNameException;

public class Define extends Operation
{
    @Override
    public void exec() throws ExecuteOperationException, InvalidNameException
    {
        if(args.length < 2)
            throw new ExecuteOperationException("Not enough arguments in define command!!!");

        if(args.length > 2)
            throw new ExecuteOperationException("Too many arguments in " + this.getClass().getSimpleName().toLowerCase() + " command!!!");

        if(args[0] instanceof Double)
            throw new InvalidNameException("" + (double)args[0]);

        CalculatorStack.createVar((String) args[0], (double)args[1]);
    }
}
