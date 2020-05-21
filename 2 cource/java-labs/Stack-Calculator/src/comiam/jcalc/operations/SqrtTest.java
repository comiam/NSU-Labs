package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.JCalculatorException;
import org.junit.Assert;
import org.junit.Test;

public class SqrtTest
{

    @Test
    public void exec() throws JCalculatorException
    {
        CalculatorStack.push(16);
        Operation a = new Sqrt();
        a.exec();
        Assert.assertTrue((int)CalculatorStack.peek() == 4);
    }
}