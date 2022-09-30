package comiam.chattree.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONCore
{
    public static <T> T parseFromJSON(String json, Class<T> tClass)
    {
        try
        {
            Gson gson = new GsonBuilder().setLenient().create();
            return gson.fromJson(json, tClass);
        }catch(Throwable e)
        {
            return null;
        }
    }

    public static <T> String saveToJSON(T obj)
    {
        Gson gson = new GsonBuilder().setLenient().create();
        return gson.toJson(obj);
    }
}