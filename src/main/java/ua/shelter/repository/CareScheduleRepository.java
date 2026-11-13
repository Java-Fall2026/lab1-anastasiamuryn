package ua.shelter.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.model.CareSchedule;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CareScheduleRepository extends GenericRepository<CareSchedule> {

    private static final Logger log = LoggerFactory.getLogger(CareScheduleRepository.class);

    public CareScheduleRepository() {
        super("schedules", CareScheduleRepository::identityOf);
    }

    private static String identityOf(CareSchedule schedule) {
        return schedule.getAnimal().getChipNumber() + "|" + schedule.getStartDate();
    }

    public List<CareSchedule> sortByEndDateNullsLast() {
        Comparator<CareSchedule> comparator = Comparator
                .comparing(CareSchedule::getEndDate, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
                .thenComparing(CareSchedule::getStartDate)
                .thenComparing(CareSchedule::getAnimal);
        return findAllSorted("end date (open-ended last), then start date, then animal", comparator);
    }

    public List<CareSchedule> sortByFeedingsDescThenKeeperName() {
        Comparator<CareSchedule> comparator = Comparator
                .comparingInt(CareSchedule::getFeedingsPerDay)
                .reversed()
                .thenComparing(schedule -> schedule.getKeeper().fullName())
                .thenComparing(Comparator.naturalOrder());
        return findAllSorted("feedings per day descending, then keeper name", comparator);
    }

    public List<CareSchedule> findByKeeperNameContaining(String part) {
        Objects.requireNonNull(part, "Keeper name part must not be null");
        String needle = part.toLowerCase();
        List<CareSchedule> result = stream()
                .filter(schedule -> schedule.getKeeper().fullName().toLowerCase().contains(needle))
                .sorted()
                .toList();
        logSearch("keeper name containing '" + part + "'", result.size());
        return result;
    }

    public List<CareSchedule> findByStartDateBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "Start date must not be null");
        Objects.requireNonNull(to, "End date must not be null");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        List<CareSchedule> result = stream()
                .filter(schedule -> !schedule.getStartDate().isBefore(from))
                .filter(schedule -> !schedule.getStartDate().isAfter(to))
                .sorted()
                .toList();
        logSearch("start date between " + from + " and " + to, result.size());
        return result;
    }

    public List<CareSchedule> findByKeeperBadgeAndMinFeedings(String badge, int minFeedings) {
        Objects.requireNonNull(badge, "Badge must not be null");
        List<CareSchedule> result = stream()
                .filter(schedule -> schedule.getKeeper().employeeBadge().equalsIgnoreCase(badge.trim()))
                .filter(schedule -> schedule.getFeedingsPerDay() >= minFeedings)
                .sorted()
                .toList();
        logSearch("keeper badge '" + badge + "' and feedings per day >= " + minFeedings, result.size());
        return result;
    }

    public List<String> findDistinctKeeperNames() {
        List<String> result = stream()
                .map(schedule -> schedule.getKeeper().fullName())
                .distinct()
                .sorted()
                .toList();
        logSearch("distinct keeper names", result.size());
        return result;
    }

    public List<CareSchedule> findMostFrequentlyFed(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must not be negative");
        }
        List<CareSchedule> result = stream()
                .sorted(Comparator.comparingInt(CareSchedule::getFeedingsPerDay).reversed()
                        .thenComparing(Comparator.naturalOrder()))
                .limit(count)
                .toList();
        logSearch("top " + count + " by feedings per day", result.size());
        return result;
    }

    public Optional<CareSchedule> findFirstByAnimalChip(String chipNumber) {
        Objects.requireNonNull(chipNumber, "Chip number must not be null");
        Optional<CareSchedule> result = stream()
                .filter(schedule -> schedule.getAnimal().getChipNumber().equalsIgnoreCase(chipNumber.trim()))
                .sorted()
                .findFirst();
        logSearch("first schedule of animal '" + chipNumber + "'", result.isPresent() ? 1 : 0);
        return result;
    }

    public Map<String, List<CareSchedule>> groupByKeeper() {
        Map<String, List<CareSchedule>> result = stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getKeeper().fullName(),
                        TreeMap::new,
                        Collectors.toList()));
        log.info("[schedules] grouped by keeper, groups: {}", result.size());
        return result;
    }

    public Map<String, Double> averageFeedingsByKeeper() {
        Map<String, Double> result = stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getKeeper().fullName(),
                        TreeMap::new,
                        Collectors.averagingDouble(CareSchedule::getFeedingsPerDay)));
        log.info("[schedules] averaged feedings by keeper, groups: {}", result.size());
        return result;
    }

    public int totalFeedingsPerDay() {
        int total = stream()
                .mapToInt(CareSchedule::getFeedingsPerDay)
                .sum();
        log.info("[schedules] total feedings per day: {}", total);
        return total;
    }

    public IntSummaryStatistics feedingsStatistics() {
        IntSummaryStatistics statistics = stream()
                .mapToInt(CareSchedule::getFeedingsPerDay)
                .summaryStatistics();
        log.info("[schedules] feedings statistics calculated over {} schedules", statistics.getCount());
        return statistics;
    }

    private void logSearch(String criterion, int found) {
        if (found == 0) {
            log.warn("[schedules] search by {} found nothing", criterion);
        } else {
            log.info("[schedules] search by {} found: {}", criterion, found);
        }
    }
}