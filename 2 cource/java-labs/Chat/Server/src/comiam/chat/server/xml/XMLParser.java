package comiam.chat.server.xml;

import comiam.chat.server.utils.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import static comiam.chat.server.messages.MessageNameConstants.DEFAULT_CONFIG_XML_HEADER_NAME;
import static comiam.chat.server.xml.XMLConstants.BAD_XML_FIELD;
import static comiam.chat.server.xml.XMLConstants.BAD_XML_HEADER;

public class XMLParser
{
    private static String errorMessage;

    public static String getParserError()
    {
        return errorMessage;
    }

    public static Pair<Pair<Integer, Boolean>, String> loadConfig(String configPath)
    {
        File file = new File(configPath);
        if(!file.exists())
        {
            errorMessage = "Configuration file doesn't exist!";
            return null;
        }

        XMLErrorHandler errorHandler = new XMLErrorHandler();

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);
            Document document = builder.parse(new FileInputStream(file));

            Element root = document.getDocumentElement();

            if(root == null)
            {
                errorMessage = "Empty xml!";
                return null;
            }

            if(!root.getNodeName().equals(DEFAULT_CONFIG_XML_HEADER_NAME))
            {
                errorMessage = "Invalid xml header!";
                return null;
            }

            NodeList list = root.getChildNodes();

            if(list.getLength() == 0)
            {
                errorMessage = "Empty xml!";
                return null;
            }

            String[] res = parseAndCheck(list, "port", "logging", "database");

            if(res[0].equals(BAD_XML_HEADER))
            {
                errorMessage = BAD_XML_HEADER;
                return null;
            }else if(res[0].equals(BAD_XML_FIELD))
            {
                errorMessage = BAD_XML_FIELD + ": " + res[1];
                return null;
            }

            return new Pair<>(new Pair<>(Integer.parseInt(res[0]), Boolean.parseBoolean(res[1])), res[2]);
        }catch (SAXException | IOException | ParserConfigurationException | NumberFormatException e)
        {
            if(e instanceof SAXException)
                errorMessage = errorHandler.getMessage();
            else if(e instanceof IOException)
                errorMessage = "Error when reading the configuration!";
            else if(e instanceof ParserConfigurationException)
                e.printStackTrace();
            else
                errorMessage = BAD_XML_FIELD + ": port";

            return null;
        }
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

        if(list.getLength() != values.length)
            return new String[] {BAD_XML_HEADER};

        String[] result = new String[values.length];

        for(int i = 0; i < values.length;i++)
        {
            if(!getNodeName(list, i).trim().equals(values[i]))
                return new String[] {BAD_XML_HEADER};

            if((result[i] = getNodeVal(list, i)).trim().isEmpty())
                return new String[] {BAD_XML_FIELD, values[i]};
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
