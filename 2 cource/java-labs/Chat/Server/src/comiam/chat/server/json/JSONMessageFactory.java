package comiam.chat.server.json;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.messages.types.MessagePackage;
import comiam.chat.server.messages.types.MessageType;
import comiam.chat.server.utils.Pair;

import java.util.ArrayList;

public class JSONMessageFactory
{
    /**
     * returns <name-chat size> list
     */
    public static String generateChatList()
    {
        ArrayList<Pair<String, Integer>> chatList = new ArrayList<>();

        for(var chat : ServerData.getChats())
            chatList.add(new Pair<>(chat.getName(), chat.getUserSize()));

        return JSONCore.saveToJSON(chatList);
    }

    /**
     * returns <name> list
     */
    public static String generateChatUsersList(Chat chat)
    {
        ArrayList<String> chatList = new ArrayList<>();

        for(var user : chat.getUsers())
            chatList.add(user.getUsername());

        return JSONCore.saveToJSON(chatList);
    }

    /**
     * returns <name-online> list
     */
    public static String generateOnlineChatUsersList(Chat chat)
    {
        ArrayList<Pair<String, String>> chatList = new ArrayList<>();

        for(var user : chat.getUsers())
            chatList.add(new Pair<>(user.getUsername(), user.isOnline() ? "Online" : "Offline"));

        return JSONCore.saveToJSON(chatList);
    }

    public static String generateChatMessageList(Chat chat)
    {
        return  JSONCore.saveToJSON(chat.getMessages());
    }

    public static String makeSuccess(String msg)
    {
        MessagePackage ans = new MessagePackage(MessageType.SUCCESS_ANSWER, msg);
        return JSONCore.saveToJSON(ans);
    }

    public static String makeFailure(String msg)
    {
        MessagePackage ans = new MessagePackage(MessageType.FAILURE_ANSWER, msg);
        return JSONCore.saveToJSON(ans);
    }

    public static String makeDisconnect()
    {
        MessagePackage ans = new MessagePackage(MessageType.DISCONNECT_NOTICE, null);
        return JSONCore.saveToJSON(ans);
    }

    public static String makeNotice(String data, MessageType type)
    {
        MessagePackage ans = new MessagePackage(type, data);
        return JSONCore.saveToJSON(ans);
    }
}
