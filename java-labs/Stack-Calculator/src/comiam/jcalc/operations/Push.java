package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.operations.ExecuteOperationException;
import comiam.jcalc.exception.stack.UnknownVariableException;

public class Push extends Operation
{
    @Override
    public void exec() throws UnknownVariableException, ExecuteOperationException
    {
        if(args.length == 0)
            throw new ExecuteOperationException("Empty argument in push command!!!");

        if(args.length > 1)
            throw new ExecuteOperationException("Too many arguments in " + this.getClass().getSimpleName().toLowerCase() + " command!!!");

        if(args[0] instanceof String)
            CalculatorStack.push((String)args[0]);
        else
            CalculatorStack.push((double)args[0]);
    }
}
