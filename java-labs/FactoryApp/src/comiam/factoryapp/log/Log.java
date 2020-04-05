package comiam.factoryapp.log;

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

    public static synchronized void init()
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

        if(file != null)
        {
            System.setProperty("log4j.configurationFile", file.getPath());
        }else
            throw new RuntimeException("Can't upload log configuration file!");

        info = LogManager.getLogger("calc-info");
        err = LogManager.getLogger("calc-error");
        debug = LogManager.getLogger("calc-debug");
    }

    public static synchronized void enableLogging()
    {
        logEnabled = true;
    }

    public static synchronized void disableLogging()
    {
        logEnabled = false;
    }

    public static synchronized void enableInfoLogging()
    {
        if(!logEnabled)
            logEnabled = true;
        infoEnabled = true;
    }

    public static synchronized void disableInfoLogging()
    {
        if(!logEnabled)
            logEnabled = true;
        infoEnabled = false;
    }

    public static synchronized void enableDebugLogging()
    {
        if(!logEnabled)
            logEnabled = true;
        debugEnabled = true;
    }

    public static synchronized void disableDebugLogging()
    {
        debugEnabled = false;
    }

    public static synchronized void enableErrorLogging()
    {
        errorEnabled = true;
    }

    public static synchronized void disableErrorLogging()
    {
        errorEnabled = false;
    }

    public static synchronized <T> void printArray(String splitter, LogType type, ArrayList<T> array)
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

        StringBuilder message = new StringBuilder("[");
        if(array.isEmpty())
            message.append("empty");
        for(int i = 0; i < array.size(); i++)
            message.append(array.get(i).toString()).append(i == array.size() - 1 ? "" : splitter + " ");
        message.append("]");

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

    public static synchronized <T> void printArray(String splitter, LogType type, T... array)
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

        StringBuilder message = new StringBuilder("[");
        if(array.length == 0)
            message.append("empty");
        for(int i = 0; i < array.length; i++)
            message.append(array[i]).append(i == array.length - 1 ? "" : splitter + " ");
        message.append("]");

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

    public static synchronized <T extends Number> void info(T message)
    {
        info(message + "");
    }

    public static synchronized void info(String message)
    {
        log(LogType.INFO, message, null);
    }

    public static synchronized <T extends Number> void debug(T message)
    {
        debug(message + "");
    }

    public static synchronized void debug(String message)
    {
        log(LogType.DEBUG, message, null);
    }

    public static synchronized <T extends Number> void error(T message)
    {
        error(message + "");
    }

    public static synchronized void error(String message)
    {
        log(LogType.ERROR, message, null);
    }

    public static synchronized void error(String message, Throwable e)
    {
        log(LogType.ERROR, message, e);
    }

    public static synchronized void log(LogType type, String message, Throwable t)
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