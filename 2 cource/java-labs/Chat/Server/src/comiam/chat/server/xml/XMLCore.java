package comiam.chat.server.xml;

import comiam.chat.server.data.units.Chat;
import comiam.chat.server.data.units.Message;
import comiam.chat.server.data.units.User;
import comiam.chat.server.logger.Log;
import comiam.chat.server.utils.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;

import static comiam.chat.server.xml.XMLConstants.*;

public class XMLCore
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

    public static Pair<ArrayList<User>, ArrayList<Chat>> loadDatabase(File file)
    {
        XMLErrorHandler errorHandler = new XMLErrorHandler();

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);
            InputSource is = new InputSource(new FileInputStream(file));
            Document document = builder.parse(is);

            Element root = document.getDocumentElement();

            if(root == null)
            {
                errorMessage = "Empty xml!";
                return null;
            }

            if(!root.getNodeName().equals(DEFAULT_DATABASE_XML_HEADER_NAME))
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

            if(list.getLength() != 2)
            {
                errorMessage = "Invalid database size!";
                return null;
            }

            if(list.item(0).getNodeName().equals(USERS_HEADER))
            {
                errorMessage = "Invalid users header!";
                return null;
            }

            if(list.item(1).getNodeName().equals(CHATS_HEADER))
            {
                errorMessage = "Invalid chats header!";
                return null;
            }

            NodeList userNodes = list.item(0).getChildNodes();
            NodeList chatNodes = list.item(1).getChildNodes();

            User usr;
            Chat cht;
            ArrayList<User> userList = new ArrayList<>();
            ArrayList<Chat> chatList = new ArrayList<>();

            for(int i = 0;i < userNodes.getLength();i++)
                if((usr = parseUser(userNodes.item(i), i)) != null)
                    userList.add(usr);
                else
                    return null;

            for(int i = 0;i < chatNodes.getLength();i++)
                if((cht = parseChat(chatNodes.item(i), userList, i)) != null)
                    chatList.add(cht);
                else
                    return null;

            return new Pair<>(userList, chatList);
        }catch (Exception e)
        {
            errorMessage = errorHandler.getMessage();
            return null;
        }
    }

    public static void saveDatabase(File file, ArrayList<User> userList, ArrayList<Chat> chatList)
    {
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(DEFAULT_DATABASE_XML_HEADER_NAME);
            document.appendChild(rootElement);

            Element userListNode = document.createElement(USERS_HEADER);
            Element userNode, userName, userPassword, userActivity;

            for(var user : userList)
            {
                userNode = document.createElement(USER_NODE_HEADER);

                userName = document.createElement(NAME_NODE);
                userName.setNodeValue(user.getUsername());
                userPassword = document.createElement(USER_PASSWORD_NODE);
                userPassword.setNodeValue(user.getPassHash());
                userActivity = document.createElement(ACTIVITY_NODE);
                userActivity.setNodeValue(user.getLastActive());

                userNode.appendChild(userName);
                userNode.appendChild(userPassword);
                userNode.appendChild(userActivity);

                userListNode.appendChild(userNode);
            }

            Element chatListNode = document.createElement(CHATS_HEADER);
            Element chatNode, messageNode, textMessage, chatUsers, chatMessages, chatDate, chatName;

            for(var chat : chatList)
            {
                chatNode = document.createElement(CHAT_NODE_HEADER);
                chatUsers = document.createElement(CHAT_USER_NODE_HEADER);
                chatMessages = document.createElement(CHAT_MESSAGES_NODE_HEADER);

                for(var user : chat.getUsers())
                {
                    userNode = document.createElement(USER_NODE_HEADER);
                    userName = document.createElement(NAME_NODE);
                    userName.setNodeValue(user.getUsername());

                    chatUsers.appendChild(userNode);
                }

                for(var message : chat.getMessages())
                {
                    messageNode = document.createElement(MESSAGE_NODE);
                    userName = document.createElement(NAME_NODE);
                    userName.setNodeValue(message.getUser().getUsername());
                    textMessage = document.createElement(TEXT_NODE);
                    textMessage.setNodeValue(message.getText());
                    userActivity = document.createElement(ACTIVITY_NODE);
                    userActivity.setNodeValue(message.getDate());

                    chatMessages.appendChild(messageNode);
                }
                chatNode.appendChild(chatUsers);
                chatNode.appendChild(chatMessages);

                chatDate = document.createElement(ACTIVITY_NODE);
                chatDate.setNodeValue(chat.getDateOfCreation());

                chatName = document.createElement(NAME_NODE);
                chatName.setNodeValue(chat.getName());

                chatNode.appendChild(chatDate);
                chatNode.appendChild(chatName);

                chatListNode.appendChild(chatNode);
            }

            writeXMLToFile(document, file);
        }catch(Exception e)
        {
            e.printStackTrace();
            Log.error("Error on saving database", e);
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

    private static User parseUser(Node user, int index)
    {
        if(!user.getNodeName().equals(USER_NODE_HEADER))
        {
            errorMessage = "Bad user node in DataBase №" + index + ": bad name!";
            return null;
        }
        NodeList nodeList = user.getChildNodes();

        if(nodeList.getLength() != 3)
        {
            errorMessage = "Bad user node in DataBase №" + index + ": invalid field size!";
            return null;
        }

        String[] tmpList = parseAndCheck(nodeList, NAME_NODE, USER_PASSWORD_NODE, ACTIVITY_NODE);

        if(tmpList[0].equals(BAD_XML_HEADER))
        {
            errorMessage = "Bad user node in DataBase №" + index + ":" + BAD_XML_HEADER;
            return null;
        }else if(tmpList[0].equals(BAD_XML_FIELD))
        {
            errorMessage = "Bad user node in DataBase №" + index + ":" + BAD_XML_FIELD + ": " + tmpList[1];
            return null;
        }

        return new User(tmpList[1], tmpList[0], tmpList[2]);
    }

    private static Chat parseChat(Node chat, ArrayList<User> userList, int index)
    {
        if(!chat.getNodeName().equals(CHAT_NODE_HEADER))
        {
            errorMessage = "Bad chat node in DataBase №" + index + ": bad name!";
            return null;
        }
        NodeList nodeList = chat.getChildNodes();

        if(nodeList.getLength() != 4)
        {
            errorMessage = "Bad chat in DataBase №" + index + ": invalid field size!";
            return null;
        }

        if(!nodeList.item(0).getNodeName().equals(CHAT_USER_NODE_HEADER))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": invalid user node name!";
            return null;
        }
        if(!nodeList.item(1).getNodeName().equals(CHAT_MESSAGES_NODE_HEADER))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": invalid message node name!";
            return null;
        }
        if(!nodeList.item(2).getNodeName().equals(ACTIVITY_NODE))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": invalid date node name!";
            return null;
        }

        if(!nodeList.item(3).getNodeName().equals(NAME_NODE))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": invalid chat name!";
            return null;
        }

        String[] tmpList = parseAndCheck(nodeList.item(2).getChildNodes(), ACTIVITY_NODE);

        if(tmpList[0].equals(BAD_XML_HEADER))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": " + BAD_XML_HEADER;
            return null;
        }else if(tmpList[0].equals(BAD_XML_FIELD))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": " + tmpList[1];
            return null;
        }

        String chatDate = tmpList[0];

        tmpList = parseAndCheck(nodeList.item(3).getChildNodes(), NAME_NODE);

        if(tmpList[0].equals(BAD_XML_HEADER))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": " + BAD_XML_HEADER;
            return null;
        }else if(tmpList[0].equals(BAD_XML_FIELD))
        {
            errorMessage = "Bad chat in DataBase №" + index + ": " + tmpList[1];
            return null;
        }

        String chatName = tmpList[0];

        ArrayList<User> chatUserList = new ArrayList<>();
        ArrayList<Message> chatMessageList = new ArrayList<>();

        NodeList chatUsers = nodeList.item(0).getChildNodes();
        Node user;

        for(int j = 0;j < chatUsers.getLength();j++)
        {
            user = chatUsers.item(j);

            if(!user.getNodeName().equals(USER_NODE_HEADER))
            {
                errorMessage = "Bad user node in chat №" + index + " with index " + j + ": bad name!";
                return null;
            }
            nodeList = user.getChildNodes();

            if(nodeList.getLength() != 1)
            {
                errorMessage = "Bad user node in chat №" + index + " with index " + j + ": invalid field size!";
                return null;
            }

            tmpList = parseAndCheck(nodeList, NAME_NODE);

            if(tmpList[0].equals(BAD_XML_HEADER))
            {
                errorMessage = "Bad user node in chat №" + index + " with index " + j + ": " + BAD_XML_HEADER;
                return null;
            }else if(tmpList[0].equals(BAD_XML_FIELD))
            {
                errorMessage = "Bad user node in chat №" + index + " with index " + j + ": " + tmpList[1];
                return null;
            }

            User foundUser = null;
            for(var usr : userList)
                if(usr.getUsername().equals(tmpList[0]))
                {
                    foundUser = usr;
                    break;
                }

            if(foundUser == null)
            {
                errorMessage = "Bad user node in chat №" + index + " with index " + j + ": this user doesn't exist in database!";
                return null;
            }
            chatUserList.add(foundUser);
        }

        NodeList chatMessages = nodeList.item(1).getChildNodes();
        Node message;

        Message chatMessage;

        for(int j = 0;j < chatMessages.getLength();j++)
        {
            message = chatMessages.item(j);

            if(!message.getNodeName().equals(MESSAGE_NODE))
            {
                errorMessage = "Bad message node in chat №" + index + " with index " + j + ": bad name!";
                return null;
            }
            nodeList = message.getChildNodes();

            if(nodeList.getLength() != 3)
            {
                errorMessage = "Bad message node in chat №" + index + " with index " + j + ": invalid field size!";
                return null;
            }

            tmpList = parseAndCheck(nodeList, TEXT_NODE, USER_NODE_HEADER, ACTIVITY_NODE);

            if(tmpList[0].equals(BAD_XML_HEADER))
            {
                errorMessage = "Bad message node in chat №" + index + " with index " + j + ": " + BAD_XML_HEADER;
                return null;
            }else if(tmpList[0].equals(BAD_XML_FIELD))
            {
                errorMessage = "Bad message node in chat №" + index + " with index " + j + ": " + tmpList[1];
                return null;
            }

            User foundUser = null;
            for(var usr : userList)
                if(usr.getUsername().equals(tmpList[1]))
                {
                    foundUser = usr;
                    break;
                }

            if(foundUser == null)
            {
                errorMessage = "Bad message node in chat №" + index + " with index " + j + ": this user doesn't exist in database!";
                return null;
            }

            chatMessage = new Message(tmpList[0], tmpList[2], foundUser);
            chatMessageList.add(chatMessage);
        }
        return new Chat(chatName, chatDate, chatUserList, chatMessageList);
    }

    private static void writeXMLToFile(Document doc, File file) throws TransformerException, FileNotFoundException
    {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(doc);
        FileOutputStream fos = new FileOutputStream(file);
        StreamResult result = new StreamResult(fos);
        tr.transform(source, result);
    }
}
