package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.operations.ExecuteOperationException;
import comiam.jcalc.exception.stack.EmptyStackException;

public class Pop extends Operation
{
    @Override
    public void exec() throws ExecuteOperationException, EmptyStackException
    {
        if(args.length > 0)
            throw new ExecuteOperationException("Too many arguments in " + this.getClass().getSimpleName().toLowerCase() + " command!!!");

        System.out.println("Popped " + CalculatorStack.pop());
    }
}
