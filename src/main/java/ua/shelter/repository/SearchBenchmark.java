package ua.shelter.repository;

import ua.shelter.model.Animal;
import ua.shelter.model.Species;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SearchBenchmark {

    private static final int SIZE = 100_000;
    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASURE_ROUNDS = 7;
    private static final int OPS_PER_ROUND = 500;

    private static long sink; 
    private SearchBenchmark() {
    }

    public static void main(String[] args) {
        
        IdentityExtractor<Animal> extractor = a -> a.getChipNumber();
        GenericRepository<Animal> repo = new GenericRepository<>("benchmark-animals", extractor);

        Species species = new Species("DOG001", "Dog", "CARNIVORE");
        List<Animal> generated = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            generated.add(Animal.of("CHIP" + i, "Pet", species, LocalDate.of(2020, 1, 1), "HEALTHY"));
        }
        repo.addAll(generated);

        
        List<Animal> list = repo.findAll();

        String first = extractor.identityOf(list.get(0));
        String last = extractor.identityOf(list.get(list.size() - 1));
        String missing = "NO-SUCH-CHIP";

        System.out.println("| Scenario | Map, ns/op | List, ns/op | List / Map |");
        System.out.println("|---|---|---|---|");
        run("first element", first, repo, list, extractor);
        run("last element", last, repo, list, extractor);
        run("missing element", missing, repo, list, extractor);
        System.out.println("(sink=" + sink + ")");
    }

    private static void run(String scenario, String id, GenericRepository<Animal> repo,
                            List<Animal> list, IdentityExtractor<Animal> extractor) {
        long map = medianNanosPerOp(repo::findByIdentity, id);
        long lin = medianNanosPerOp(key -> linearSearch(list, key, extractor), id);
        double ratio = map == 0 ? Double.NaN : (double) lin / map;
        System.out.printf("| %s | %d | %d | %.2fx |%n", scenario, map, lin, ratio);
    }

    private static <E> long medianNanosPerOp(Function<String, Optional<E>> lookup, String id) {
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            runRound(lookup, id);
        }
        long[] results = new long[MEASURE_ROUNDS];
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            results[i] = runRound(lookup, id);
        }
        Arrays.sort(results);
        return results[MEASURE_ROUNDS / 2];
    }

    private static <E> long runRound(Function<String, Optional<E>> lookup, String id) {
        long start = System.nanoTime();
        int hits = 0;
        for (int i = 0; i < OPS_PER_ROUND; i++) {
            if (lookup.apply(id).isPresent()) {
                hits++;
            }
        }
        long elapsed = System.nanoTime() - start;
        sink += hits;
        return elapsed / OPS_PER_ROUND;
    }

    private static <E> Optional<E> linearSearch(
            List<E> items, String identity, IdentityExtractor<E> extractor) {
        for (E item : items) {
            if (identity.equals(extractor.identityOf(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}