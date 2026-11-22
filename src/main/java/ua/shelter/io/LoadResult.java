package ua.shelter.io;

import java.util.ArrayList;
import java.util.List;

public record LoadResult(int read, int added, List<Rejection> rejected) {

    public record Rejection(String source, int recordNumber, String field, String message) {

        @Override
        public String toString() {
            if (recordNumber <= 0) {
                return source + ": " + message;
            }
            return source + ", запис №" + recordNumber + ", поле '" + field + "': " + message;
        }
    }

    public LoadResult {
        rejected = List.copyOf(rejected);
    }

         public int rejectedCount() {
        return (int) rejected.stream()
                .map(r -> r.source() + "#" + r.recordNumber())
                .distinct()
                .count();
    }

    public LoadResult merge(LoadResult other) {
        List<Rejection> all = new ArrayList<>(rejected);
        all.addAll(other.rejected);
        return new LoadResult(read + other.read, added + other.added, all);
    }
}