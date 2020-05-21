package comiam.jcalc.operations;

import comiam.jcalc.exception.JCalculatorException;

public abstract class Operation
{
    protected Object[] args = new Object[0];

    public Operation setArgs(Object... args)
    {
        this.args = args;
        return this;
    }

    public abstract void exec() throws JCalculatorException;
}
