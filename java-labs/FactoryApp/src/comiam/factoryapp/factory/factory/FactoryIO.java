package comiam.factoryapp.factory.factory;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class FactoryIO
{
    public static Factory readFactory(File file)
    {
        XMLStreamReader xmlReader = null;
        try
        {
            int accessorySupplierCount = -1;
            int producerCount = -1;
            int dealerCount = -1;
            int supplierDelay = -1;
            int producerDelay = -1;
            int dealerDelay = -1;
            int accessoryStoreLimit = -1;
            int engineStoreLimit = -1;
            int bodyworkStoreLimit = -1;
            int carStoreLimit = -1;
            boolean logBoolIsInitialized = false;
            boolean loggingEnabled = false;

            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(file));
            while (xmlReader.hasNext())
            {
                int event = xmlReader.next();
                if (event == XMLEvent.START_ELEMENT)
                {
                    switch(xmlReader.getLocalName())
                    {
                        case "AccessorySupplierCount":
                            accessorySupplierCount = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "ProducerCount":
                            producerCount = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "DealerCount":
                            dealerCount = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "SupplierDelay":
                            supplierDelay = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "ProducerDelay":
                            producerDelay = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "DealerDelay":
                            dealerDelay = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "AccessoryStoreLimit":
                            accessoryStoreLimit = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "EngineStoreLimit":
                            engineStoreLimit = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "BodyworkStoreLimit":
                            bodyworkStoreLimit = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "CarStoreLimit":
                            carStoreLimit = Integer.parseInt(xmlReader.getElementText());
                            break;
                        case "LoggingEnabled":
                            loggingEnabled = Boolean.parseBoolean(xmlReader.getElementText());
                            logBoolIsInitialized = true;
                            break;
                        default:
                    }
                }
            }

            xmlReader.close();

            if(accessorySupplierCount == -1 ||
               producerCount == -1 ||
               dealerCount == -1 ||
               supplierDelay == -1 ||
               producerDelay == -1 ||
               dealerDelay == -1 ||
               accessoryStoreLimit == -1 ||
               engineStoreLimit == -1 ||
               bodyworkStoreLimit == -1 ||
               carStoreLimit == -1 ||
               !logBoolIsInitialized)
                throw new IllegalArgumentException("Bad XML Configuration file!");

            return new Factory(accessorySupplierCount, producerCount, dealerCount, supplierDelay, producerDelay, dealerDelay, accessoryStoreLimit, engineStoreLimit, bodyworkStoreLimit, carStoreLimit, loggingEnabled);
        }catch(IOException | XMLStreamException | IllegalArgumentException ex)
        {
            if(xmlReader != null)
                try
                {
                    xmlReader.close();
                } catch(XMLStreamException ignored) {}
        }
        return null;
    }

    public static boolean saveFactory(File file, Factory factory)
    {
        XMLStreamWriter writer = null;
        try
        {
            StringWriter sw = new StringWriter();
            XMLOutputFactory output = XMLOutputFactory.newInstance();
            writer = output.createXMLStreamWriter(sw);

            // Open XML-doc and write FactoryConfiguration
            writer.writeStartDocument("1.0");
            writer.writeStartElement("FactoryConfiguration");

            writer.writeStartElement("AccessorySupplierCount");
            writer.writeCharacters("" + factory.getAccessorySupplierCount());
            writer.writeEndElement();

            writer.writeStartElement("ProducerCount");
            writer.writeCharacters("" + factory.getProducerCount());
            writer.writeEndElement();

            writer.writeStartElement("DealerCount");
            writer.writeCharacters("" + factory.getDealerCount());
            writer.writeEndElement();

            writer.writeStartElement("SupplierDelay");
            writer.writeCharacters("" + factory.getSupplierDelay());
            writer.writeEndElement();

            writer.writeStartElement("ProducerDelay");
            writer.writeCharacters("" + factory.getProducerDelay());
            writer.writeEndElement();

            writer.writeStartElement("DealerDelay");
            writer.writeCharacters("" + factory.getDealerDelay());
            writer.writeEndElement();

            writer.writeStartElement("AccessoryStoreLimit");
            writer.writeCharacters("" + factory.getAccessoryStore().getLimit());
            writer.writeEndElement();

            writer.writeStartElement("EngineStoreLimit");
            writer.writeCharacters("" + factory.getEngineStore().getLimit());
            writer.writeEndElement();

            writer.writeStartElement("BodyworkStoreLimit");
            writer.writeCharacters("" + factory.getBodyworkStore().getLimit());
            writer.writeEndElement();

            writer.writeStartElement("CarStoreLimit");
            writer.writeCharacters("" + factory.getCarStore().getLimit());
            writer.writeEndElement();

            writer.writeStartElement("LoggingEnabled");
            writer.writeCharacters("" + factory.isLoggingEnabled());
            writer.writeEndElement();

            // Close root node
            writer.writeEndElement();
            // Close XML doc
            writer.writeEndDocument();
            writer.flush();
            writer.close();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter formattedStringWriter = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(sw.toString())), new StreamResult(formattedStringWriter));

            PrintStream stream = new PrintStream(file);
            stream.print(formattedStringWriter);
            return true;
        } catch(XMLStreamException | IOException | TransformerException ex)
        {
            ex.printStackTrace();
            if(writer != null)
                try
                {
                    writer.close();
                } catch(XMLStreamException ignored) {}
        }
        return false;
    }
}


