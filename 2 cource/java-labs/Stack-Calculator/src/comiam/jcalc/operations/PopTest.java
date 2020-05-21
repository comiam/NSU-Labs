package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.JCalculatorException;
import org.junit.Assert;
import org.junit.Test;

public class PopTest
{

    @Test
    public void exec() throws JCalculatorException
    {
        CalculatorStack.push(89);
        CalculatorStack.push(90);
        Operation a = new Pop();
        a.exec();
        Assert.assertTrue((int)CalculatorStack.peek() == 89);
    }
}