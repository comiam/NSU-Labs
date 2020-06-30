package comiam.chat.server.json;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.messages.types.Answer;
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
     * returns <name-activity> list
     */
    public static String generateChatUsersList(Chat chat)
    {
        ArrayList<Pair<String, String>> chatList = new ArrayList<>();

        for(var user : chat.getUsers())
            chatList.add(new Pair<>(user.getUsername(), user.getLastActive()));

        return JSONCore.saveToJSON(chatList);
    }

    /**
     * returns <name-online> list
     */
    public static String generateOnlineChatUsersList(Chat chat)
    {
        ArrayList<Pair<String, String>> chatList = new ArrayList<>();

        for(var user : chat.getUsers())
            chatList.add(new Pair<>(user.getUsername(), user.getLastActive().equals("Online") ? "Online" : "Offline"));

        return JSONCore.saveToJSON(chatList);
    }

    public static String generateChatMessageList(Chat chat)
    {
        return  JSONCore.saveToJSON(chat.getMessages());
    }

    public static String makeSuccess(String msg)
    {
        Answer ans = new Answer(msg, true);
        return JSONCore.saveToJSON(ans);
    }

    public static String makeFailure(String msg)
    {
        Answer ans = new Answer(msg, false);
        return JSONCore.saveToJSON(ans);
    }
}
