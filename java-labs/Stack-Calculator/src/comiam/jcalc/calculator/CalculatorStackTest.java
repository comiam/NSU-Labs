package comiam.jcalc.calculator;

import comiam.jcalc.exception.stack.EmptyStackException;
import comiam.jcalc.exception.stack.UnknownVariableException;
import org.junit.Assert;
import org.junit.Test;

public class CalculatorStackTest
{

    @Test
    public void getVarCount() throws EmptyStackException
    {
        CalculatorStack.clear();

        long arg0 = CalculatorStack.getVarCount();
        Assert.assertEquals(arg0, 0);

        CalculatorStack.push(45);
        CalculatorStack.push(45);
        arg0 = CalculatorStack.getVarCount();
        Assert.assertEquals(arg0, 2);

        CalculatorStack.pop();
        arg0 = CalculatorStack.getVarCount();
        Assert.assertEquals(arg0, 1);

        CalculatorStack.pop();
        arg0 = CalculatorStack.getVarCount();
        Assert.assertEquals(arg0, 0);
    }

    @Test
    public void createVar() throws UnknownVariableException, EmptyStackException
    {
        CalculatorStack.clear();

        CalculatorStack.createVar("azaza", 56);
        CalculatorStack.push("azaza");
        double arg0  = CalculatorStack.peek();
        Assert.assertEquals((int)arg0, (int)56.0);

        CalculatorStack.createVar("azaza", 23);
        arg0  = CalculatorStack.peek();
        Assert.assertEquals((int)arg0, (int)23.0);
    }

    @Test
    public void push() throws UnknownVariableException, EmptyStackException
    {
        CalculatorStack.clear();

        CalculatorStack.createVar("azaza", 56);
        CalculatorStack.push("azaza");
        double arg0  = CalculatorStack.peek();
        Assert.assertEquals((int)arg0, (int)56.0);

        CalculatorStack.push(90);
        arg0  = CalculatorStack.peek();
        Assert.assertEquals((int)arg0, (int)90.0);
    }

    @Test
    public void pop() throws UnknownVariableException, EmptyStackException
    {
        CalculatorStack.clear();

        CalculatorStack.createVar("azaza", 56);
        CalculatorStack.push("azaza");
        long size0 = CalculatorStack.getVarCount();
        double arg0  = CalculatorStack.pop();
        long size1 = CalculatorStack.getVarCount();
        Assert.assertEquals((int)arg0, (int)56.0);
        Assert.assertEquals((int)size1, (int)size0 - 1);

        CalculatorStack.push(90);
        size0 = CalculatorStack.getVarCount();
        arg0  = CalculatorStack.pop();
        size1 = CalculatorStack.getVarCount();
        Assert.assertEquals((int)arg0, (int)90.0);
        Assert.assertEquals((int)size1, (int)size0 - 1);
    }

    @Test
    public void peek() throws UnknownVariableException, EmptyStackException
    {
        CalculatorStack.clear();

        CalculatorStack.createVar("azaza", 56);
        CalculatorStack.push("azaza");
        double arg0  = CalculatorStack.peek();
        Assert.assertEquals((int)arg0, (int)56.0);

        CalculatorStack.createVar("azaza", 23);
        arg0  = CalculatorStack.peek();
        Assert.assertEquals((int)arg0, (int)23.0);
    }
}