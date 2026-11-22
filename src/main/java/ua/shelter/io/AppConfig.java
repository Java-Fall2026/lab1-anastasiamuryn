package ua.shelter.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

public class AppConfig {

    public static final String DEFAULT_RESOURCE = "app.properties";

    private final Properties properties;

    public AppConfig(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "Properties must not be null");
    }

    public static AppConfig fromClasspath() throws IOException {
        return fromClasspath(DEFAULT_RESOURCE);
    }

    public static AppConfig fromClasspath(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("Configuration resource not found: " + resourceName);
            }
            properties.load(in);
        }
        return new AppConfig(properties);
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new NoSuchElementException("Missing required property: " + key);
        }
        return value.trim();
    }

    public Path path(String key) {
        return Path.of(get(key));
    }
}