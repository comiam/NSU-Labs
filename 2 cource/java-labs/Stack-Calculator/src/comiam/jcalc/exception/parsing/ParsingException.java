package comiam.jcalc.exception.parsing;

import comiam.jcalc.exception.JCalculatorException;

public class ParsingException extends JCalculatorException
{
    public ParsingException() {
        super();
    }
    public ParsingException(String s) {
        super(s);
    }
}
