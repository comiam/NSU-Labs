package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.JCalculatorException;
import org.junit.Assert;
import org.junit.Test;

public class MinusTest
{

    @Test
    public void exec() throws JCalculatorException
    {
        CalculatorStack.push(90);
        CalculatorStack.push(90);
        Operation a = new Minus();
        a.exec();
        Assert.assertTrue((int)CalculatorStack.peek() == 0);
    }
}