package ua.shelter.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.exception.DuplicateIdentityException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores elements of any type, keyed by an identity string.
 * Uniqueness is enforced here, not in the entity classes:
 * Species, Animal and Keeper know nothing about duplicates.
 *
 * @param <T> element type
 */
public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;

    // LinkedHashMap: O(1) lookup like HashMap, and findAll() keeps insertion order.
    // HashMap: order not guaranteed. TreeMap: sorted by identity, but O(log n).
    private final Map<String, T> items = new LinkedHashMap<>();

    public GenericRepository(String name, IdentityExtractor<T> extractor) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Repository name must not be blank");
        }
        this.name = name;
        this.extractor = Objects.requireNonNull(extractor, "Extractor must not be null");
    }

    /** Adds an element; throws DuplicateIdentityException if its identity is already present. */
    public void add(T item) {
        String identity = put(item);
        log.info("[{}] added: {}", name, identity);
    }

    /** Adds an element only if its identity is absent; never throws on a duplicate. */
    public boolean addIfAbsent(T item) {
        String identity = requireIdentity(item);
        if (items.putIfAbsent(identity, item) != null) {
            log.warn("[{}] duplicate rejected: {}", name, identity);
            return false;
        }
        log.info("[{}] added: {}", name, identity);
        return true;
    }

    /**
     * Adds all elements and logs one summary line instead of one line per element
     * (the benchmark adds 100 000 of them). Not atomic: stops at the first duplicate,
     * elements before it stay added.
     * {@code ? extends T}: the collection only gives us elements, so a subtype collection is fine.
     */
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

    // Not logged here: the caller logs "found" / "not found" (one event, one place),
    // and the benchmark calls this method hundreds of thousands of times.
    public Optional<T> findByIdentity(String identity) {
        return Optional.ofNullable(items.get(identity));
    }

    // Unmodifiable copy: add/remove on it throws UnsupportedOperationException.
    // The elements themselves are shared, so a mutable object can still be changed through it.
    // That is why the identity must come from a field that never changes.
    public List<T> findAll() {
        return List.copyOf(items.values());
    }

    public int size() {
        return items.size();
    }

    // Validates and stores the element; logs WARN and throws on a duplicate.
    // Success is not logged here: add() and addAll() log it in their own way.
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