package ua.shelter.repository;

@FunctionalInterface
public interface IdentityExtractor<T> {
    String identityOf(T item);
}
