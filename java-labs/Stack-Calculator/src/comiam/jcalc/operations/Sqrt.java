package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.operations.ExecuteOperationException;
import comiam.jcalc.exception.stack.EmptyStackException;

public class Sqrt extends Operation
{
    @Override
    public void exec() throws EmptyStackException, ExecuteOperationException
    {
        if(args.length > 0)
            throw new ExecuteOperationException("Too many arguments in " + this.getClass().getSimpleName().toLowerCase() + " command!!!");
        if(CalculatorStack.getVarCount() == 0)
            throw new ExecuteOperationException("Not enough variables in stack!!!");
        double valFirst = CalculatorStack.pop();
        CalculatorStack.push(Math.sqrt(valFirst));
    }
}
