package org.nsu.fit.services.log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Attachment;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

public class Logger {
    private static final org.apache.log4j.Logger LOGGER;

    static {
        PropertyConfigurator.configure(Logger.class.getClassLoader().getResourceAsStream("log4.properties"));
        LOGGER = org.apache.log4j.Logger.getLogger(Logger.class.getName());
    }

    public static org.apache.log4j.Logger getLogger() {
        return LOGGER;
    }

    public static void error(String message, Throwable t) {
        log(Level.ERROR, message, t);
    }

    public static void debug(String message, Throwable t) {
        log(Level.DEBUG, message, t);
    }

    public static void warn(String message, Throwable t) {
        log(Level.WARN, message, t);
    }

    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    public static void warn(String message) {
        log(Level.WARN, message, null);
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    private static void log(Priority priority, String message, Throwable throwable) {
        getLogger().log(priority, message, throwable);

        String messageForAttachment = "[" + priority + "]: " + message;

        if (throwable != null) {
            message += "\n\n" + throwable.toString();
        }

        attachMessage(
                messageForAttachment.substring(0, Math.min(messageForAttachment.length(), 80)),
                message);
    }

    public static <T> String toJson(T value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        String json = mapper.writeValueAsString(value);

        Object obj = mapper.readValue(json, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    @Attachment(value = "{0}", type = "text/plain")
    private static String attachMessage(String attachName, String message) {
        return message;
    }
}
