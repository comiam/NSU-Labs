package comiam.chat.client.xml;

import comiam.chat.client.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

import static comiam.chat.client.xml.XMLConstants.*;

public class XMLCore
{
    private static String errorMessage;

    public static String getParserError()
    {
        return errorMessage;
    }

    public static Pair<String, Document> decodeXML(String xml)
    {
        XMLErrorHandler errorHandler = new XMLErrorHandler();

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);

            return new Pair<>(null, document);
        }catch (Exception e)
        {
            return new Pair<>(errorHandler.getMessage(), null);
        }
    }

    public static String[] parseAndCheck(NodeList list, String... values)
    {
        if(values == null || values.length == 0)
            return null;

        if(list.getLength() - values.length - 1 != values.length)
            return new String[] {BAD_XML_HEADER};

        String[] result = new String[values.length];

        int realIndex = 0;
        for(int i = 0; i < list.getLength();i++)
            if(list.item(i).getNodeType() != Node.TEXT_NODE)
            {
                if(!getNodeName(list, i).equals(values[realIndex]))
                    return new String[] {BAD_XML_HEADER};

                if((result[realIndex] = getNodeVal(list, i)).isEmpty())
                    return new String[] {BAD_XML_FIELD, values[realIndex]};

                realIndex++;
            }

        return result;
    }

    public static String getNodeVal(NodeList list, int index)
    {
        return list.item(index).getTextContent().trim();
    }

    public static String getNodeName(NodeList list, int index)
    {
        return list.item(index).getNodeName().trim();
    }
}
