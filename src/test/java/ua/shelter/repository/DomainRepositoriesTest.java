package ua.shelter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DomainRepositoriesTest {

    private static final Species DOG = new Species("DOG001", "Dog", "CARNIVORE");
    private static final Species CAT = new Species("CAT002", "Cat", "CARNIVORE");
    private static final Species RABBIT = new Species("RAB003", "Rabbit", "HERBIVORE");

    private static final Keeper IVAN = new Keeper("K100", "Ivan Petrenko", LocalDate.of(2015, 3, 10));
    private static final Keeper OLENA = new Keeper("K200", "Olena Shevchenko", LocalDate.of(2018, 9, 1));

    private AnimalRepository animalRepo;
    private CareScheduleRepository scheduleRepo;

    private Animal bobDog;
    private Animal rexDog;
    private Animal mistyCat;
    private Animal bobCat;
    private Animal lunaRabbit;

    @BeforeEach
    void setUp() {
        animalRepo = new AnimalRepository();
        scheduleRepo = new CareScheduleRepository();

        bobDog = Animal.of("CHIP001", "Bob", DOG, LocalDate.of(2016, 5, 23), "HEALTHY");
        rexDog = Animal.of("CHIP002", "Rex", DOG, LocalDate.of(2019, 2, 14), "UNDER_TREATMENT");
        mistyCat = Animal.of("CHIP003", "Misty", CAT, LocalDate.of(2021, 7, 1), "HEALTHY");
        bobCat = Animal.of("CHIP004", "Bob", CAT, LocalDate.of(2022, 11, 30), "QUARANTINE");
        lunaRabbit = Animal.of("CHIP005", "Luna", RABBIT, LocalDate.of(2023, 4, 12), "HEALTHY");

        animalRepo.addAll(List.of(bobDog, rexDog, mistyCat, bobCat, lunaRabbit));

        scheduleRepo.addAll(List.of(
                CareSchedule.of(bobDog, IVAN, LocalDate.of(2020, 1, 10), null, 3),
                CareSchedule.of(rexDog, IVAN, LocalDate.of(2021, 3, 1), LocalDate.of(2024, 3, 1), 2),
                CareSchedule.of(mistyCat, OLENA, LocalDate.of(2022, 5, 15), null, 4),
                CareSchedule.of(bobCat, OLENA, LocalDate.of(2023, 1, 20), LocalDate.of(2023, 12, 31), 3),
                CareSchedule.of(lunaRabbit, OLENA, LocalDate.of(2023, 6, 1), LocalDate.of(2025, 6, 1), 5)
        ));
    }

    @Test
    @DisplayName("1. Natural order sorts animals by nickname and then by chip number")
    void naturalOrderSortsAnimalsCorrectly() {
        List<Animal> list = new ArrayList<>(animalRepo.findAll());
        Collections.sort(list);

        assertEquals(List.of(bobDog, bobCat, lunaRabbit, mistyCat, rexDog), list);
    }

    @Test
    @DisplayName("2. Multi-level comparator orders items with identical first key by secondary keys")
    void multiLevelComparatorOrdersBySecondaryKeys() {
        List<Animal> sorted = animalRepo.sortByHealthThenYoungestFirst();

        assertEquals(List.of(lunaRabbit, mistyCat, bobDog, bobCat, rexDog), sorted);
    }

    @Test
    @DisplayName("3. findAllSortedByIdentity in ASC and DESC produce mutually reversed lists")
    void findAllSortedByIdentityProducesMutuallyReversedLists() {
        List<Animal> asc = animalRepo.findAllSortedByIdentity(SortOrder.ASC);
        List<Animal> desc = animalRepo.findAllSortedByIdentity(SortOrder.DESC);

        assertEquals(asc.reversed(), desc);
    }

    @Test
    @DisplayName("4. Double.compare distinguishes fractional values differing by less than 1.0")
    void fractionalValuesLessThanOneAreSortedProperly() {
        List<Double> values = new ArrayList<>(List.of(2.9, 2.1, 2.5));
        values.sort(Double::compare);

        assertEquals(List.of(2.1, 2.5, 2.9), values);
    }

    @Test
    @DisplayName("5. TreeSet with comparator inconsistent with equals treats equal-key items as duplicates")
    void treeSetWithInconsistentComparatorIgnoresSecondEqualKeyItem() {
        TreeSet<Animal> byNickname = new TreeSet<>(Comparator.comparing(Animal::getNickname));

        assertNotEquals(bobDog, bobCat);
        assertTrue(byNickname.add(bobDog));
        assertFalse(byNickname.add(bobCat));
        assertEquals(1, byNickname.size());
    }

    @Test
    @DisplayName("6. Range search excludes items outside boundary dates")
    void rangeSearchExcludesOutOfBoundsDates() {
        List<Animal> result = animalRepo.findByBirthDateBetween(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2022, 12, 31)
        );

        assertEquals(List.of(rexDog, mistyCat, bobCat), result);
        assertFalse(result.contains(bobDog));
        assertFalse(result.contains(lunaRabbit));
    }

    @Test
    @DisplayName("7. Combined search excludes items matching only one condition")
    void combinedSearchRequiresBothConditions() {
        List<Animal> result = animalRepo.findBySpeciesAndHealth("DOG001", "HEALTHY");

        assertEquals(List.of(bobDog), result);
        assertFalse(result.contains(rexDog)); 
        assertFalse(result.contains(mistyCat)); 
    }

    @Test
    @DisplayName("8. Single-object search returns Optional.empty() when no match exists")
    void singleSearchReturnsEmptyOptionalWhenNotFound() {
        Optional<Animal> result = animalRepo.findFirstBySpecies("UNKNOWN999");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("9. groupingBy produces expected group count and group sizes")
    void groupingByProducesExpectedGroupsAndSizes() {
        Map<String, List<Animal>> bySpecies = animalRepo.groupBySpecies();

        assertEquals(3, bySpecies.size());
        assertEquals(2, bySpecies.get("Dog").size());
        assertEquals(2, bySpecies.get("Cat").size());
        assertEquals(1, bySpecies.get("Rabbit").size());
    }

    @Test
    @DisplayName("10. Aggregation on empty repository does not fail and returns meaningful defaults")
    void aggregationOnEmptyRepositoryReturnsSafeDefaults() {
        CareScheduleRepository emptyRepo = new CareScheduleRepository();

        assertEquals(0, emptyRepo.totalFeedingsPerDay());
        IntSummaryStatistics stats = emptyRepo.feedingsStatistics();
        assertEquals(0, stats.getCount());
        assertEquals(0, stats.getSum());
    }

    @Test
    @DisplayName("11. Reusing a consumed stream throws IllegalStateException")
    void reusingConsumedStreamThrowsException() {
        Stream<Animal> stream = animalRepo.stream();
        assertEquals(5, stream.count());

        assertThrows(IllegalStateException.class, stream::count);
    }

    @Test
    @DisplayName("12. NullsLast comparator places open-ended schedules at the end")
    void nullsLastComparatorPlacesNullEndDatesLast() {
        List<CareSchedule> sorted = scheduleRepo.sortByEndDateNullsLast();

        assertNull(sorted.get(3).getEndDate());
        assertNull(sorted.get(4).getEndDate());
    }
}