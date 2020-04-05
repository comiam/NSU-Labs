package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.operations.ExecuteOperationException;
import comiam.jcalc.exception.stack.EmptyStackException;

public class Multiply extends Operation
{
    @Override
    public void exec() throws EmptyStackException, ExecuteOperationException
    {
        if(args.length > 0)
            throw new ExecuteOperationException("Too many arguments in " + this.getClass().getSimpleName().toLowerCase() + " command!!!");
        if(CalculatorStack.getVarCount() < 2)
            throw new ExecuteOperationException("Not enough variables in stack!!!");
        double valFirst = CalculatorStack.pop();
        double valSecond = CalculatorStack.pop();
        CalculatorStack.push(valFirst * valSecond);
    }
}
