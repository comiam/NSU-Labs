package comiam.jcalc.operations;

import comiam.jcalc.calculator.CalculatorStack;
import comiam.jcalc.exception.JCalculatorException;
import org.junit.Assert;
import org.junit.Test;

public class DefineTest
{
    @Test
    public void exec() throws JCalculatorException
    {
        Operation a = new Define();
        a.setArgs("aaw", 90.0);
        a.exec();
        CalculatorStack.push("aaw");
        Assert.assertTrue((int)CalculatorStack.peek() == 90);
    }
}