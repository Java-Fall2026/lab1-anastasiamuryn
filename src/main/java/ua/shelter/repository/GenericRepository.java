package ua.shelter.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.exception.DuplicateIdentityException;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;

    private final Map<String, T> items = new LinkedHashMap<>();

    public GenericRepository(String name, IdentityExtractor<T> extractor) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Repository name must not be blank");
        }
        this.name = name;
        this.extractor = Objects.requireNonNull(extractor, "Extractor must not be null");
    }

    public void add(T item) {
        String identity = put(item);
        log.info("[{}] added: {}", name, identity);
    }

    public boolean addIfAbsent(T item) {
        String identity = requireIdentity(item);
        if (items.putIfAbsent(identity, item) != null) {
            log.warn("[{}] duplicate rejected: {}", name, identity);
            return false;
        }
        log.info("[{}] added: {}", name, identity);
        return true;
    }

    public void addAll(Collection<? extends T> newItems) {
        if (newItems == null) {
            log.error("[{}] rejected null collection", name);
            throw new IllegalArgumentException("Collection must not be null");
        }
        for (T item : newItems) {
            put(item);
        }
        log.info("[{}] added items: {}", name, newItems.size());
    }

    public boolean removeByIdentity(String identity) {
        if (items.remove(identity) == null) {
            log.warn("[{}] nothing to remove: {}", name, identity);
            return false;
        }
        log.info("[{}] removed: {}", name, identity);
        return true;
    }

    public Optional<T> findByIdentity(String identity) {
        return Optional.ofNullable(items.get(identity));
    }

    public List<T> findAll() {
        return List.copyOf(items.values());
    }

    public int size() {
        return items.size();
    }

    public List<T> findAllSorted(Comparator<T> comparator) {
        return findAllSorted("custom comparator", comparator);
    }

    public List<T> findAllSorted(String criterion, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");
        List<T> result = items.values().stream()
                .sorted(comparator)
                .toList();
        log.info("[{}] sorted by {}, returned: {}", name, criterion, result.size());
        return result;
    }

    public List<T> findAllSortedByIdentity(SortOrder order) {
        Objects.requireNonNull(order, "Sort order must not be null");
        List<T> result = items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(order.identityComparator()))
                .map(Map.Entry::getValue)
                .toList();
        log.info("[{}] sorted by identity {}, returned: {}", name, order, result.size());
        return result;
    }

    public Stream<T> stream() {
        return items.values().stream();
    }

    private String put(T item) {
        String identity = requireIdentity(item);
        if (items.containsKey(identity)) {
            log.warn("[{}] duplicate rejected: {}", name, identity);
            throw new DuplicateIdentityException(identity, name);
        }
        items.put(identity, item);
        return identity;
    }

    private String requireIdentity(T item) {
        if (item == null) {
            log.error("[{}] rejected null item", name);
            throw new IllegalArgumentException("Item must not be null");
        }
        String identity = extractor.identityOf(item);
        if (identity == null || identity.isBlank()) {
            log.error("[{}] rejected item with blank identity", name);
            throw new IllegalArgumentException("Identity must not be blank");
        }
        return identity;
    }
}