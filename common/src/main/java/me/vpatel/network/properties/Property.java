package me.vpatel.network.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Property<T> {

    private static final Logger log = LogManager.getLogger(Properties.class);
    private static Properties properties;

    static {
        load("convo.properties");
    }

    public static void load(String def) {
        properties = new Properties();
        String propFileLocation = System.getProperty("convo.property.location", def);
        File file = new File(propFileLocation);

        if (file.exists() && file.canRead()) {
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                properties.load(reader);
            } catch (FileNotFoundException e) {
                log.warn("Error while loading properties from file {}: {}: {}", propFileLocation, e.getClass().getName(), e.getMessage());
            } catch (IOException e) {
                log.warn("Error while loading properties from file {}: {}: {}", propFileLocation, e.getClass().getName(), e.getMessage());
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ignored) {
                }
            }
        } else {
            log.warn("Couldn't read properties from file {}", propFileLocation);
        }
    }

    private final String name;
    private final Class<T> type;
    private final T defaultValue;

    public Property(String name, Class<T> type, T defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public T get() {
        Object value = getValue();

        try {
            if (String.class.equals(type)) {
                //noinspection unchecked
                return (T) value.toString();
            } else if (Integer.class.equals(type)) {
                //noinspection unchecked
                return (T) Integer.valueOf(value.toString());
            } else if (Float.class.equals(type)) {
                //noinspection unchecked
                return (T) Float.valueOf(value.toString());
            } else if (Double.class.equals(type)) {
                //noinspection unchecked
                return (T) Double.valueOf(value.toString());
            } else if (Short.class.equals(type)) {
                //noinspection unchecked
                return (T) Short.valueOf(value.toString());
            }
        } catch (Exception ex) {
            throw new PropertyException("Can't convert " + value + " to type " + type.getName(), ex);
        }
        throw new PropertyException("Type " + type.getName() + " isn't supported yet");
    }

    private Object getValue() {
        Object value = System.getProperty(name);
        if (value != null) {
            return value;
        }

        value = System.getenv(name);
        if (value != null) {
            return value;
        }

        if (properties == null) {
            return defaultValue;
        }

        value = properties.getProperty(name);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public static class PropertyException extends RuntimeException {

        public PropertyException(String message) {
            super(message);
        }

        public PropertyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}