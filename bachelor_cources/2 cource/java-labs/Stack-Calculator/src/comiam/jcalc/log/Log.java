package comiam.jcalc.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class Log
{
    private static Logger info;
    private static Logger err;
    private static Logger debug;
    private static boolean infoEnabled = true;
    private static boolean debugEnabled = true;
    private static boolean errorEnabled = true;
    private static boolean logEnabled = false;

    public static void init()
    {
        File file = null;
        String resource = "/res/log4j2.xml";

        try
        {
            InputStream input = Log.class.getResourceAsStream(resource);
            file = File.createTempFile("tempfile", ".tmp");
            OutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            while((read = input.read(bytes)) != -1)
                out.write(bytes, 0, read);
            out.close();
            file.deleteOnExit();
        } catch(IOException ex)
        {
            ex.printStackTrace();
        }

        System.setProperty("log4j.configurationFile", file.getPath());


        info = LogManager.getLogger("calc-info");
        err = LogManager.getLogger("calc-error");
        debug = LogManager.getLogger("calc-debug");
    }

    public static void enableLogging()
    {
        logEnabled = true;
    }

    public static void disableLogging()
    {
        logEnabled = false;
    }

    public static void enableInfoLogging()
    {
        infoEnabled = true;
    }

    public static void disableInfoLogging()
    {
        infoEnabled = false;
    }

    public static void enableDebugLogging()
    {
        debugEnabled = true;
    }

    public static void disableDebugLogging()
    {
        debugEnabled = false;
    }

    public static void enableErrorLogging()
    {
        errorEnabled = true;
    }

    public static void disableErrorLogging()
    {
        errorEnabled = false;
    }

    public static <T> void printArray(String splitter, LogType type, ArrayList<T> array)
    {
        if(array == null)
        {
            error("Null array!");
            return;
        }

        if(splitter == null)
        {
            error("Null splitter!");
            return;
        }

        String message = "[";
        if(array.isEmpty())
            message += "empty";
        for(int i = 0; i < array.size(); i++)
            message += array.get(i).toString() + (i == array.size() - 1 ? "" : splitter + " ");
        message += "]";

        switch(type)
        {
            case INFO:
                info(message + "");
                break;
            case DEBUG:
                debug(message + "");
                break;
            case ERROR:
                error(message + "");
                break;
        }
    }

    public static <T> void printArray(String splitter, LogType type, T... array)
    {
        if(array == null)
        {
            error("Null array!");
            return;
        }

        if(splitter == null)
        {
            error("Null splitter!");
            return;
        }

        String message = "[";
        if(array.length == 0)
            message += "empty";
        for(int i = 0; i < array.length; i++)
            message += array[i] + (i == array.length - 1 ? "" : splitter + " ");
        message += "]";

        switch(type)
        {
            case INFO:
                info(message + "");
                break;
            case DEBUG:
                debug(message + "");
                break;
            case ERROR:
                error(message + "");
                break;
        }
    }

    public static <T extends Number> void info(T message)
    {
        info(message + "");
    }

    public static void info(String message)
    {
        log(LogType.INFO, message, null);
    }

    public static <T extends Number> void debug(T message)
    {
        debug(message + "");
    }

    public static void debug(String message)
    {
        log(LogType.DEBUG, message, null);
    }

    public static <T extends Number> void error(T message)
    {
        error(message + "");
    }

    public static void error(String message)
    {
        log(LogType.ERROR, message, null);
    }

    public static void error(String message, Throwable e)
    {
        log(LogType.ERROR, message, e);
    }

    public static void log(LogType type, String message, Throwable t)
    {
        if(!logEnabled)
            return;
        switch(type)
        {
            case DEBUG:
                if(!debugEnabled)
                    return;
                debug.debug(message);
                break;
            case ERROR:
                if(!errorEnabled)
                    return;
                err.warn(message, t);
                break;
            case INFO:
                if(!infoEnabled)
                    return;
                info.info(message);
                break;
        }
    }

    public enum LogType
    {
        INFO,
        DEBUG,
        ERROR
    }
}
