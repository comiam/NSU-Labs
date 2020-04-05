package comiam.jcalc.exception.stack;

public class UnknownVariableException extends CalcStackException
{
    public UnknownVariableException() {
        super();
    }
    public UnknownVariableException(String s) {
        super(s);
    }
}
