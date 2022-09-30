package comiam.chat.server.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import comiam.chat.server.logger.Log;

import java.io.*;
import java.lang.reflect.Type;

public class JSONCore
{
    public static <T> T parseFromFile(String file, Type classType)
    {
        try
        {
            Gson gson = new GsonBuilder().create();
            JsonReader reader = new JsonReader(new FileReader(file));

            return gson.fromJson(reader, classType);
        }catch(Throwable e)
        {
            return null;
        }
    }

    public static <T> void saveToFile(File file, T obj)
    {
        try (Writer writer = new FileWriter(file))
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(obj, writer);
        } catch(IOException e)
        {
            e.printStackTrace();
            Log.error("Error on saving database", e);
        }
    }

    public static <T> T parseFromJSON(String json, Class<T> tClass)
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
