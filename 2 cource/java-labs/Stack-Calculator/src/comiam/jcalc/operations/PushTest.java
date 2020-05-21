package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.JCalculatorException;
import org.junit.Assert;
import org.junit.Test;

public class PushTest
{

    @Test
    public void exec() throws JCalculatorException
    {
        Operation a = new Push();
        a.setArgs(90.0);
        a.exec();
        Assert.assertTrue((int)CalculatorStack.peek() == 90);
    }
}