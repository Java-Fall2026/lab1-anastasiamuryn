package ua.shelter.io;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class FileStorage<T> {

    private final Class<T> type;

    public FileStorage(Class<T> type) {
        this.type = Objects.requireNonNull(type, "Type must not be null");
    }

    public List<T> load(Path file) throws IOException {
        Objects.requireNonNull(file, "File must not be null");
        ObjectMapper mapper = DataFormat.fromPath(file).createMapper();
        List<T> result = mapper.readValue(file.toFile(), listType(mapper));
        return result == null ? List.of() : result;
    }

    public void save(Path file, List<T> items) throws IOException {
        Objects.requireNonNull(file, "File must not be null");
        Objects.requireNonNull(items, "Items must not be null");
        ObjectMapper mapper = DataFormat.fromPath(file).createMapper();
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        mapper.writerFor(listType(mapper)).writeValue(file.toFile(), items);
    }

    private JavaType listType(ObjectMapper mapper) {
        return mapper.getTypeFactory().constructCollectionType(List.class, type);
    }
}