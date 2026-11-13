package ua.shelter.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.model.Animal;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AnimalRepository extends GenericRepository<Animal> {

    private static final Logger log = LoggerFactory.getLogger(AnimalRepository.class);

    public AnimalRepository() {
        super("animals", Animal::getChipNumber);
    }

    public List<Animal> sortByNicknameIgnoreCase() {
        return findAllSorted(
                "nickname ignoring case",
                (a, b) -> a.getNickname().compareToIgnoreCase(b.getNickname()));
    }

    public List<Animal> sortByHealthThenYoungestFirst() {
        Comparator<Animal> comparator = Comparator.comparing(Animal::getHealth)
                .thenComparing(Comparator.comparing(Animal::getBirthDate).reversed())
                .thenComparing(Animal::getChipNumber);
        return findAllSorted("health, then birth date descending, then chip number", comparator);
    }

    public List<Animal> findByNicknameContaining(String part) {
        Objects.requireNonNull(part, "Nickname part must not be null");
        String needle = part.toLowerCase();
        List<Animal> result = stream()
                .filter(animal -> animal.getNickname().toLowerCase().contains(needle))
                .sorted()
                .toList();
        logSearch("nickname containing '" + part + "'", result.size());
        return result;
    }

    public List<Animal> findByBirthDateBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "Start date must not be null");
        Objects.requireNonNull(to, "End date must not be null");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        List<Animal> result = stream()
                .filter(animal -> !animal.getBirthDate().isBefore(from))
                .filter(animal -> !animal.getBirthDate().isAfter(to))
                .sorted(Comparator.comparing(Animal::getBirthDate))
                .toList();
        logSearch("birth date between " + from + " and " + to, result.size());
        return result;
    }

    public List<Animal> findBySpeciesAndHealth(String speciesCode, String health) {
        Objects.requireNonNull(speciesCode, "Species code must not be null");
        Objects.requireNonNull(health, "Health must not be null");
        List<Animal> result = stream()
                .filter(animal -> animal.getSpecies().getSpeciesCode().equalsIgnoreCase(speciesCode.trim()))
                .filter(animal -> animal.getHealth().equalsIgnoreCase(health.trim()))
                .sorted()
                .toList();
        logSearch("species '" + speciesCode + "' and health '" + health + "'", result.size());
        return result;
    }

    public List<Animal> findYoungest(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative");
        }
        List<Animal> result = stream()
                .sorted(Comparator.comparing(Animal::getBirthDate).reversed())
                .limit(count)
                .toList();
        logSearch("youngest " + count, result.size());
        return result;
    }

    public List<String> findDistinctNicknames() {
        List<String> result = stream()
                .map(Animal::getNickname)
                .distinct()
                .sorted()
                .toList();
        logSearch("distinct nicknames", result.size());
        return result;
    }

    public Optional<Animal> findFirstBySpecies(String speciesCode) {
        Objects.requireNonNull(speciesCode, "Species code must not be null");
        Optional<Animal> result = stream()
                .filter(animal -> animal.getSpecies().getSpeciesCode().equalsIgnoreCase(speciesCode.trim()))
                .findFirst();
        logSearch("first animal of species '" + speciesCode + "'", result.isPresent() ? 1 : 0);
        return result;
    }

    public Map<String, List<Animal>> groupBySpecies() {
        Map<String, List<Animal>> result = stream()
                .collect(Collectors.groupingBy(
                        animal -> animal.getSpecies().getCommonName(),
                        TreeMap::new,
                        Collectors.toList()));
        log.info("[animals] grouped by species, groups: {}", result.size());
        return result;
    }

    public Map<String, Long> countByHealth() {
        Map<String, Long> result = stream()
                .collect(Collectors.groupingBy(
                        Animal::getHealth,
                        TreeMap::new,
                        Collectors.counting()));
        log.info("[animals] counted by health, groups: {}", result.size());
        return result;
    }

    private void logSearch(String criterion, int found) {
        if (found == 0) {
            log.warn("[animals] search by {} found nothing", criterion);
        } else {
            log.info("[animals] search by {} found: {}", criterion, found);
        }
    }
}