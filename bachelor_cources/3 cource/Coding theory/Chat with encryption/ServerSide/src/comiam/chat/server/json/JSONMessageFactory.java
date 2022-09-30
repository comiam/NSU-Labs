package comiam.chat.server.json;

import comiam.chat.server.data.ServerData;
import comiam.chat.server.data.units.Chat;
import comiam.chat.server.messages.types.ErrorType;
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
        ArrayList<Pair<String, String>> chatList = new ArrayList<>();

        for(var user : chat.getUsers())
        {
            String lastActive = "null";

            for(int i = chat.getMessages().size() - 1; i >= 0; i--)
            {
                var msg = chat.getMessages().get(i);
                if(msg.getUsername().equals(user.getUsername()) ||
                        msg.getUsername().equals(ServerData.getServerNotifier().getUsername()) && msg.getText().contains(" " + user.getUsername() + " "))
                {
                    lastActive = msg.getDate();
                    break;
                }
            }

            chatList.add(new Pair<>(user.getUsername(), lastActive));
        }

        return JSONCore.saveToJSON(chatList);
    }

    public static String generateChatMessageList(Chat chat, boolean forBroadcast)
    {
        return JSONCore.saveToJSON(forBroadcast ? new Pair<>(chat.getName(), chat.getMessages()) : chat.getMessages());
    }

    public static String makeSuccess(String msg)
    {
        MessagePackage ans = new MessagePackage(MessageType.SUCCESS_ANSWER, msg);
        return JSONCore.saveToJSON(ans);
    }

    public static String makeFailure(String errorType)
    {
        MessagePackage ans = new MessagePackage(MessageType.FAILURE_ANSWER, errorType);
        return JSONCore.saveToJSON(ans);
    }

    public static String makeNotice(String data, MessageType type)
    {
        MessagePackage ans = new MessagePackage(type, data);
        return JSONCore.saveToJSON(ans);
    }
}
