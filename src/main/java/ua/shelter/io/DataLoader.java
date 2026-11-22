package ua.shelter.io;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.repository.GenericRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final FileStorage<T> storage;
    private final GenericRepository<T> repository;
    private final Validator validator;

    public DataLoader(Class<T> type, GenericRepository<T> repository, Validator validator) {
        this.storage = new FileStorage<>(type);
        this.repository = Objects.requireNonNull(repository, "Repository must not be null");
        this.validator = Objects.requireNonNull(validator, "Validator must not be null");
    }

    public LoadResult load(Path file) {
        Objects.requireNonNull(file, "File must not be null");
        String source = file.getFileName().toString();

        List<T> items;
        try {
            items = storage.load(file);
        } catch (IOException e) {
            log.error("[{}] file could not be read, read: 0, added: 0, rejected: 1, cause: {}",
                    source, firstLine(e.getMessage()));
            LoadResult.Rejection rejection = new LoadResult.Rejection(
                    source, 0, "", "Файл не вдалося прочитати: " + firstLine(e.getMessage()));
            return new LoadResult(0, 0, List.of(rejection));
        }

        List<LoadResult.Rejection> rejected = new ArrayList<>();
        int added = 0;
        int rejectedRecords = 0;
        int number = 0;

        for (T item : items) {
            number++;
            List<LoadResult.Rejection> problems = findProblems(item, source, number);
            if (problems.isEmpty()) {
                if (repository.addIfAbsent(item)) {
                    added++;
                    continue;
                }
                problems = List.of(new LoadResult.Rejection(source, number, "identity",
                        "Дублікат ідентичності: такий запис уже є в репозиторії"));
            }
            rejectedRecords++;
            rejected.addAll(problems);
            log.warn("[{}] record {} rejected: {}", source, number, describe(problems));
        }

        log.info("[{}] read: {}, added: {}, rejected: {}", source, items.size(), added, rejectedRecords);
        return new LoadResult(items.size(), added, rejected);
    }

    private List<LoadResult.Rejection> findProblems(T item, String source, int number) {
        if (item == null) {
            return List.of(new LoadResult.Rejection(source, number, "record", "Порожній запис"));
        }
        return validator.validate(item).stream()
                .sorted(Comparator.comparing((ConstraintViolation<T> v) -> v.getPropertyPath().toString())
                        .thenComparing(ConstraintViolation::getMessage))
                .map(v -> new LoadResult.Rejection(source, number, fieldName(v), v.getMessage()))
                .toList();
    }

    private static String fieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        return path.isEmpty() ? "record" : path;
    }

    private static String describe(List<LoadResult.Rejection> problems) {
        return problems.stream()
                .map(p -> p.field() + ": " + p.message())
                .collect(Collectors.joining("; "));
    }

    private static String firstLine(String text) {
        if (text == null) {
            return "unknown error";
        }
        int end = text.indexOf('\n');
        return (end < 0 ? text : text.substring(0, end)).trim();
    }
}