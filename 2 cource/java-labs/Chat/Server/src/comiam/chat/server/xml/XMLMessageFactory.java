package comiam.chat.server.xml;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.Sessions;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.logger.Log;
import comiam.chat.server.messages.MessageNameConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;

import static comiam.chat.server.messages.MessageNameConstants.SUCCESS_MESSAGE;

public class XMLMessageFactory
{
    public static String generateChatListMessage()
    {
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(SUCCESS_MESSAGE);
            document.appendChild(rootElement);

            Element chatList = document.createElement("chatlist");

            Element chat, chatName, chatSize, chatCreateDate;

            ArrayList<Chat> chats;
            synchronized((chats = ServerData.getChats()))
            {
                for(var ch : chats)
                {
                    chat = document.createElement("chat");
                    chatName = document.createElement("name");
                    chatName.setNodeValue(ch.getName());
                    chatSize = document.createElement("size");
                    chatSize.setNodeValue(ch.getUserSize() + "");
                    chatCreateDate = document.createElement("date");
                    chatCreateDate.setNodeValue(ch.getDateOfCreation());
                    chat.appendChild(chatName);
                    chat.appendChild(chatSize);
                    chat.appendChild(chatCreateDate);

                    chatList.appendChild(chat);
                }
            }

            rootElement.appendChild(chatList);

            return writeXML(document);
        } catch (ParserConfigurationException e)
        {
            Log.error("MessageFactory: Unexpected error on creating chat list message!", e);
            return null;
        }
    }

    public static String generateChatUsersListMessage(Chat chat)
    {
        if(chat == null)
            return null;

        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(SUCCESS_MESSAGE);
            document.appendChild(rootElement);

            Element userList = document.createElement("userlist");

            Element user, userName, userActivity;

            synchronized(chat)
            {
                for(var usr : chat.getUsers())
                {
                    user = document.createElement("user");
                    userName = document.createElement("name");
                    userName.setNodeValue(usr.getUsername());
                    userActivity = document.createElement("activity");
                    userActivity.setNodeValue(usr.getLastActive());
                    user.appendChild(userName);
                    user.appendChild(userActivity);

                    userList.appendChild(user);
                }
            }

            rootElement.appendChild(userList);

            return writeXML(document);
        }catch(ParserConfigurationException e)
        {
            Log.error("MessageFactory: Unexpected error on creating users chat list message!", e);
            return null;
        }
    }

    public static String generateOnlineChatUsersListMessage(Chat chat)
    {
        if(chat == null)
            return null;

        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(SUCCESS_MESSAGE);
            document.appendChild(rootElement);

            Element userList = document.createElement("userlist");

            Element user, userName, userActivity;

            synchronized(chat)
            {
                for(var usr : chat.getUsers())
                {
                    user = document.createElement("user");
                    userName = document.createElement("name");
                    userName.setNodeValue(usr.getUsername());
                    userActivity = document.createElement("online");
                    userActivity.setNodeValue("" + Sessions.isUserAuthorized(usr));
                    user.appendChild(userName);
                    user.appendChild(userActivity);

                    userList.appendChild(user);
                }
            }

            rootElement.appendChild(userList);

            return writeXML(document);
        }catch(ParserConfigurationException e)
        {
            Log.error("MessageFactory: Unexpected error on creating users chat list message!", e);
            return null;
        }
    }

    public static String generateChatMessageListMessage(Chat chat)
    {
        if(chat == null)
            return null;

        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(SUCCESS_MESSAGE);
            document.appendChild(rootElement);

            Element chatList = document.createElement("messagelist");

            Element message, messageText, messageDate;

            synchronized(chat)
            {
                for(var msg : chat.getMessages())
                {
                    message = document.createElement("message");
                    messageText = document.createElement("text");
                    messageDate = document.createElement("date");
                    messageDate.setNodeValue(msg.getDate());
                    messageText.setNodeValue(msg.getText());

                    message.appendChild(messageText);
                    message.appendChild(messageDate);
                    chatList.appendChild(message);
                }
            }

            rootElement.appendChild(chatList);

            return writeXML(document);
        }catch(ParserConfigurationException e)
        {
            Log.error("MessageFactory: Unexpected error on creating message chat list message!", e);
            return null;
        }
    }

    public static String generateChatMessageListMessage(String chatName)
    {
        return generateChatMessageListMessage(ServerData.getChatByName(chatName));
    }

    public static String generateOnlineChatUsersListMessage(String chatName)
    {
        return generateOnlineChatUsersListMessage(ServerData.getChatByName(chatName));
    }

    public static String generateChatUsersListMessage(String chatName)
    {
        return generateChatUsersListMessage(ServerData.getChatByName(chatName));
    }

    public static String generateNoticeMessage(String message, int messageCount)
    {
        return XMLMessageFactory.generateSimpleMessage(MessageNameConstants.NOTICE_MESSAGE + ";" + messageCount, message);
    }

    public static String generateSimpleMessage(String messageType, String message)
    {
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(messageType);
            rootElement.setNodeValue(message);
            document.appendChild(rootElement);

            return writeXML(document);
        } catch (ParserConfigurationException e)
        {
            Log.error("MessageFactory: Unexpected error on creating simple message!", e);
            return null;
        }
    }

    private static String writeXML(Document doc)
    {
        try
        {
            var tr = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            tr.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();
        }catch (Throwable ignored)
        {
            return null;
        }
    }
}
