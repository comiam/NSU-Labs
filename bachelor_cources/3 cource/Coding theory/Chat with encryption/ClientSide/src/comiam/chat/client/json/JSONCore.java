package comiam.chat.client.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JSONCore
{
    public static <T> T parseFromJSON(String json, Type tClass)
    {
        try
        {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(json, tClass);
        }catch(Throwable e)
        {
            return null;
        }
    }

    public static <T> String saveToJSON(T obj)
    {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(obj);
    }
}
