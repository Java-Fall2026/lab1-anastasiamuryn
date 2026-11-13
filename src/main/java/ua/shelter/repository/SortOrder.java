package ua.shelter.repository;

import java.util.Comparator;

public enum SortOrder {
    ASC(Comparator.naturalOrder()),
    DESC(Comparator.reverseOrder());

    private final Comparator<String> identityComparator;

    SortOrder(Comparator<String> identityComparator) {
        this.identityComparator = identityComparator;
    }

    public Comparator<String> identityComparator() {
        return identityComparator;
    }
}