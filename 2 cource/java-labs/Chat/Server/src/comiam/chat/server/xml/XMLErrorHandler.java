package comiam.chat.server.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class XMLErrorHandler implements ErrorHandler
{
    private String message = "";

    public void warning(SAXParseException e)
    {
        message += e.getMessage() + "\n";
    }

    public void error(SAXParseException e)
    {
        message += e.getMessage() + "\n";
    }

    public void fatalError(SAXParseException e)
    {
        message += e.getMessage() + "\n";
    }

    public String getMessage()
    {
        return message;
    }
}
