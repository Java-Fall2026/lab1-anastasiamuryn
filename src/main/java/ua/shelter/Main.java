package ua.shelter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.exception.DuplicateIdentityException;
import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;
import ua.shelter.repository.AnimalRepository;
import ua.shelter.repository.CareScheduleRepository;
import ua.shelter.repository.SortOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Species dog = new Species("DOG001", "Dog", "CARNIVORE");
        Species cat = new Species("CAT002", "Cat", "CARNIVORE");
        Species rabbit = new Species("RAB003", "Rabbit", "HERBIVORE");

        Keeper ivan = new Keeper("K100", "Ivan Petrenko", LocalDate.of(2015, 3, 10));
        Keeper olena = new Keeper("K200", "Olena Shevchenko", LocalDate.of(2018, 9, 1));

        Animal bob = Animal.of("DFV46FH", "Bob", dog, LocalDate.of(2016, 5, 23), "HEALTHY");
        Animal rex = Animal.of("CHIP002", "Rex", dog, LocalDate.of(2019, 2, 14), "UNDER_TREATMENT");
        Animal misty = Animal.of("CHIP003", "Misty", cat, LocalDate.of(2021, 7, 1), "HEALTHY");
        Animal bobCat = Animal.of("CHIP004", "Bob", cat, LocalDate.of(2022, 11, 30), "QUARANTINE");
        Animal luna = Animal.of("CHIP005", "Luna", rabbit, LocalDate.of(2023, 4, 12), "HEALTHY");

        CareSchedule bobCare = CareSchedule.of(bob, ivan, LocalDate.of(2020, 1, 10), null, 3);
        CareSchedule rexCare = CareSchedule.of(rex, ivan, LocalDate.of(2021, 3, 1), LocalDate.of(2024, 3, 1), 2);
        CareSchedule mistyCare = CareSchedule.of(misty, olena, LocalDate.of(2022, 5, 15), null, 4);
        CareSchedule bobCatCare = CareSchedule.of(bobCat, olena, LocalDate.of(2023, 1, 20), LocalDate.of(2023, 12, 31), 3);
        CareSchedule lunaCare = CareSchedule.of(luna, olena, LocalDate.of(2023, 6, 1), LocalDate.of(2025, 6, 1), 5);

        System.out.println("=== 1. Наповнення двох репозиторіїв ===");
        AnimalRepository animalRepo = new AnimalRepository();
        CareScheduleRepository scheduleRepo = new CareScheduleRepository();
        animalRepo.addAll(List.of(bob, rex, misty, bobCat, luna));
        scheduleRepo.addAll(List.of(bobCare, rexCare, mistyCare, bobCatCare, lunaCare));
        System.out.println("Тварин: " + animalRepo.size() + ", графіків догляду: " + scheduleRepo.size());

        System.out.println("\n=== 2. Сортування ===");
        List<Animal> naturalAnimals = new ArrayList<>(animalRepo.findAll());
        Collections.sort(naturalAnimals);
        printAnimals("Тварини, природний порядок (кличка, потім чіп)", naturalAnimals);
        List<CareSchedule> naturalSchedules = new ArrayList<>(scheduleRepo.findAll());
        Collections.sort(naturalSchedules);
        printSchedules("Графіки, природний порядок (дата початку, потім тварина)", naturalSchedules);
        printAnimals("Тварини за кличкою без урахування регістру", animalRepo.sortByNicknameIgnoreCase());
        printAnimals("Тварини: здоров'я, потім наймолодші першими (багаторівневий)", animalRepo.sortByHealthThenYoungestFirst());
        printSchedules("Графіки: дата завершення, відкриті в кінці (nullsLast)", scheduleRepo.sortByEndDateNullsLast());
        printSchedules("Графіки: годувань на день за спаданням, потім ім'я працівника", scheduleRepo.sortByFeedingsDescThenKeeperName());

        System.out.println("\n=== 3. Сортування за ідентифікатором ===");
        printAnimals("ASC", animalRepo.findAllSortedByIdentity(SortOrder.ASC));
        printAnimals("DESC", animalRepo.findAllSortedByIdentity(SortOrder.DESC));

        System.out.println("\n=== 4. TreeSet з компаратором, неузгодженим з equals ===");
        TreeSet<Animal> byNickname = new TreeSet<>(Comparator.comparing(Animal::getNickname));
        boolean firstAdded = byNickname.add(bob);
        boolean secondAdded = byNickname.add(bobCat);
        System.out.println("equals для двох тварин з кличкою Bob: " + bob.equals(bobCat));
        System.out.println("Додано першого: " + firstAdded + ", додано другого: " + secondAdded);
        System.out.println("Розмір TreeSet: " + byNickname.size() + " (у репозиторії обидві тварини різні)");

        System.out.println("\n=== 5. Пошук ===");
        printAnimals("Кличка містить 'ob'", animalRepo.findByNicknameContaining("ob"));
        printAnimals("Народжені між 2016-01-01 і 2020-12-31",
                animalRepo.findByBirthDateBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2020, 12, 31)));
        printAnimals("Вид DOG001 і здоров'я HEALTHY", animalRepo.findBySpeciesAndHealth("DOG001", "HEALTHY"));
        printAnimals("Двоє наймолодших", animalRepo.findYoungest(2));
        System.out.println("Унікальні клички: " + animalRepo.findDistinctNicknames());
        printAnimals("Вид ZZZ999 (порожній результат)", animalRepo.findBySpeciesAndHealth("ZZZ999", "HEALTHY"));
        printSchedules("Працівник містить 'petr'", scheduleRepo.findByKeeperNameContaining("petr"));
        printSchedules("Початок між 2021-01-01 і 2022-12-31",
                scheduleRepo.findByStartDateBetween(LocalDate.of(2021, 1, 1), LocalDate.of(2022, 12, 31)));
        printSchedules("Працівник K200 і щонайменше 4 годування", scheduleRepo.findByKeeperBadgeAndMinFeedings("K200", 4));
        printSchedules("Двоє графіків із найбільшою кількістю годувань", scheduleRepo.findMostFrequentlyFed(2));
        System.out.println("Унікальні працівники: " + scheduleRepo.findDistinctKeeperNames());

        System.out.println("\n=== 6. Optional ===");
        String missingSpecies = animalRepo.findFirstBySpecies("ZZZ999")
                .map(Animal::getNickname)
                .orElse("тварини такого виду немає");
        System.out.println("orElse + map: " + missingSpecies);
        String missingSchedule = scheduleRepo.findFirstByAnimalChip("NOPE")
                .map(CareSchedule::toString)
                .orElseGet(() -> "графіка для чіпа NOPE немає");
        System.out.println("orElseGet: " + missingSchedule);
        animalRepo.findFirstBySpecies("CAT002")
                .ifPresent(animal -> System.out.println("ifPresent: перший кіт -> " + describe(animal)));
        try {
            animalRepo.findFirstBySpecies("ZZZ999")
                    .orElseThrow(() -> new NoSuchElementException("No animal of species ZZZ999"));
        } catch (NoSuchElementException e) {
            log.warn("Optional is empty: {}", e.getMessage());
            System.out.println("orElseThrow: перехоплено виняток -> " + e.getMessage());
        }

        System.out.println("\n=== 7. Групування й агрегація ===");
        Map<String, List<Animal>> bySpecies = animalRepo.groupBySpecies();
        bySpecies.forEach((species, animals) ->
                System.out.println("Вид " + species + ": " + animals.size() + " -> " + nicknames(animals)));
        System.out.println("Кількість тварин за здоров'ям: " + animalRepo.countByHealth());
        Map<String, List<CareSchedule>> byKeeper = scheduleRepo.groupByKeeper();
        byKeeper.forEach((keeper, schedules) ->
                System.out.println("Працівник " + keeper + ": графіків " + schedules.size()));
        System.out.println("Середня кількість годувань за працівником: " + scheduleRepo.averageFeedingsByKeeper());
        System.out.println("Сума годувань на день: " + scheduleRepo.totalFeedingsPerDay());
        IntSummaryStatistics statistics = scheduleRepo.feedingsStatistics();
        System.out.println("Статистика годувань: min=" + statistics.getMin() + ", max=" + statistics.getMax()
                + ", середнє=" + statistics.getAverage());
        CareScheduleRepository emptyRepo = new CareScheduleRepository();
        System.out.println("Агрегація на порожньому репозиторії: сума=" + emptyRepo.totalFeedingsPerDay()
                + ", кількість=" + emptyRepo.feedingsStatistics().getCount());

        System.out.println("\n=== 8. Повторне використання стріму ===");
        Stream<Animal> stream = animalRepo.stream();
        System.out.println("Перший прохід, кількість: " + stream.count());
        try {
            stream.count();
        } catch (IllegalStateException e) {
            log.error("Stream reuse failed: {}", e.getMessage());
            System.out.println("Другий прохід: перехоплено виняток -> " + e.getMessage());
        }

        System.out.println("\n=== 9. Логування ===");
        try {
            animalRepo.add(Animal.of("DFV46FH", "Bobby", dog, LocalDate.of(2016, 5, 23), "HEALTHY"));
        } catch (DuplicateIdentityException e) {
            System.out.println("Дублікат: " + e.getMessage());
        }
        try {
            animalRepo.add(null);
        } catch (IllegalArgumentException e) {
            System.out.println("add(null): " + e.getMessage());
        }
        System.out.println("(INFO -- успішні операції, сортування й пошук із результатом; "
                + "WARN -- порожній результат пошуку та дублікати; ERROR -- null та повторне використання стріму; див. лог вище)");
    }

    private static String describe(Animal animal) {
        return animal.getNickname() + " [" + animal.getChipNumber() + "]";
    }

    private static String describe(CareSchedule schedule) {
        String end = schedule.getEndDate() == null ? "триває" : schedule.getEndDate().toString();
        return schedule.getAnimal().getNickname() + " " + schedule.getStartDate() + ".." + end
                + ", годувань: " + schedule.getFeedingsPerDay()
                + ", працівник: " + schedule.getKeeper().fullName();
    }

    private static String nicknames(Collection<Animal> animals) {
        return animals.stream()
                .map(Animal::getNickname)
                .collect(Collectors.joining(", "));
    }

    private static void printAnimals(String title, Collection<Animal> animals) {
        System.out.println(title + " (" + animals.size() + "):");
        animals.forEach(animal -> System.out.println("  " + describe(animal)));
    }

    private static void printSchedules(String title, Collection<CareSchedule> schedules) {
        System.out.println(title + " (" + schedules.size() + "):");
        schedules.forEach(schedule -> System.out.println("  " + describe(schedule)));
    }
}