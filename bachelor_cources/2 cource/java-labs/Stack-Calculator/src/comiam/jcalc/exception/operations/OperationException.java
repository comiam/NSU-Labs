package comiam.jcalc.exception.operations;

import comiam.jcalc.exception.JCalculatorException;

public class OperationException extends JCalculatorException
{
    public OperationException() {
        super();
    }
    public OperationException(String s) {
        super(s);
    }
}
