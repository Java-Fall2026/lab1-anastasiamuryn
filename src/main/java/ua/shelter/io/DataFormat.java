package ua.shelter.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public enum DataFormat {
    JSON(JsonFactory::new, "json"),
    YAML(YAMLFactory::new, "yaml", "yml");

    private final Supplier<JsonFactory> factory;
    private final List<String> extensions;

    DataFormat(Supplier<JsonFactory> factory, String... extensions) {
        this.factory = factory;
        this.extensions = List.of(extensions);
    }

    public ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper(factory.get());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    public static DataFormat fromPath(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        for (DataFormat format : values()) {
            if (format.extensions.contains(extension)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported file extension '" + extension + "' in file: " + name);
    }
}