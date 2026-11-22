package ua.shelter.io;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;
import ua.shelter.repository.AnimalRepository;
import ua.shelter.repository.CareScheduleRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataLoaderTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private AnimalRepository repo;
    private DataLoader<Animal> loader;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @BeforeEach
    void setUp() {
        repo = new AnimalRepository();
        loader = new DataLoader<>(Animal.class, repo, validator);
    }

    private static String animal(String chip, String nickname, String birthDate, String health) {
        return """
                {
                  "chipNumber": "%s",
                  "nickname": "%s",
                  "species": { "speciesCode": "DOG001", "commonName": "Dog", "diet": "CARNIVORE" },
                  "birthDate": "%s",
                  "health": "%s"
                }""".formatted(chip, nickname, birthDate, health);
    }

    private static String array(String... items) {
        return "[" + String.join(",", items) + "]";
    }

    private static Set<String> fieldsOfRecord(LoadResult result, int recordNumber) {
        return result.rejected().stream()
                .filter(r -> r.recordNumber() == recordNumber)
                .map(LoadResult.Rejection::field)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Invalid records are rejected with all their violations and only valid ones reach the repository")
    void invalidRecordsAreRejected(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("animals.json");
        Files.writeString(file, array(
                animal("CHIP001", "Bob", "2020-01-01", "HEALTHY"),
                animal("CHIP002", "A", "2020-01-01", "SICK"),
                animal("CHIP003", "Rex", "2999-01-01", "HEALTHY")));

        LoadResult result = loader.load(file);

        assertEquals(3, result.read());
        assertEquals(1, result.added());
        assertEquals(2, result.rejectedCount());
        assertEquals(3, result.rejected().size());
        assertEquals(List.of("CHIP001"), repo.findAll().stream().map(Animal::getChipNumber).toList());
        assertEquals(Set.of("nickname", "health"), fieldsOfRecord(result, 2));
        assertEquals(Set.of("birthDate"), fieldsOfRecord(result, 3));
    }

    @Test
    @DisplayName("A duplicate identity goes to the rejected list instead of throwing an exception")
    void duplicateIsRejectedWithoutException(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("animals.json");
        Files.writeString(file, array(
                animal("CHIP001", "Bob", "2020-01-01", "HEALTHY"),
                animal("chip001", "Bobby", "2021-02-02", "HEALTHY")));

        LoadResult result = loader.load(file);

        assertEquals(2, result.read());
        assertEquals(1, result.added());
        assertEquals(1, result.rejectedCount());
        assertEquals("identity", result.rejected().get(0).field());
        assertEquals(2, result.rejected().get(0).recordNumber());
        assertTrue(result.rejected().get(0).message().contains("Дублікат"));
        assertEquals(1, repo.size());
        assertEquals("Bob", repo.findByIdentity("CHIP001").orElseThrow().getNickname());
    }

    @Test
    @DisplayName("A file with a syntax error does not crash the loading and adds nothing")
    void syntaxErrorDoesNotCrash(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, "[ { \"chipNumber\": \"CHIP001\", ");

        LoadResult result = loader.load(file);

        assertEquals(0, result.read());
        assertEquals(0, result.added());
        assertEquals(1, result.rejectedCount());
        assertEquals("broken.json", result.rejected().get(0).source());
        assertEquals(0, result.rejected().get(0).recordNumber());
        assertEquals(0, repo.size());
    }

    @Test
    @DisplayName("A missing file does not crash the loading and the next files are still loaded")
    void missingFileDoesNotStopOtherFiles(@TempDir Path tempDir) throws IOException {
        Path missing = tempDir.resolve("missing.json");
        Path broken = tempDir.resolve("broken.json");
        Path good = tempDir.resolve("good.json");
        Files.writeString(broken, "[ not json");
        Files.writeString(good, array(animal("CHIP001", "Bob", "2020-01-01", "HEALTHY")));

        LoadResult missingResult = loader.load(missing);
        LoadResult brokenResult = loader.load(broken);
        LoadResult goodResult = loader.load(good);

        assertEquals(1, missingResult.rejectedCount());
        assertEquals("missing.json", missingResult.rejected().get(0).source());
        assertEquals(1, brokenResult.rejectedCount());
        assertEquals(1, goodResult.added());
        assertEquals(0, goodResult.rejectedCount());
        assertEquals(1, repo.size());
    }

    @Test
    @DisplayName("YAML file: a schedule that breaks the custom annotation is rejected, a valid one is added")
    void yamlScheduleBreakingCustomAnnotationIsRejected(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("schedules.yaml");
        Files.writeString(file, """
                - animal: {chipNumber: CHIP001, nickname: Bob, species: {speciesCode: DOG001, commonName: Dog, diet: CARNIVORE}, birthDate: "2020-01-01", health: HEALTHY}
                  keeper: {employeeBadge: K100, fullName: "Ivan Petrenko", hireDate: "2015-03-10"}
                  startDate: "2022-06-01"
                  endDate: "2022-01-01"
                  feedingsPerDay: 2

                - animal: {chipNumber: CHIP001, nickname: Bob, species: {speciesCode: DOG001, commonName: Dog, diet: CARNIVORE}, birthDate: "2020-01-01", health: HEALTHY}
                  keeper: {employeeBadge: K100, fullName: "Ivan Petrenko", hireDate: "2015-03-10"}
                  startDate: "2022-01-10"
                  endDate: null
                  feedingsPerDay: 3
                """);
        CareScheduleRepository scheduleRepo = new CareScheduleRepository();
        DataLoader<CareSchedule> scheduleLoader = new DataLoader<>(CareSchedule.class, scheduleRepo, validator);

        LoadResult result = scheduleLoader.load(file);

        assertEquals(2, result.read());
        assertEquals(1, result.added());
        assertEquals(1, result.rejectedCount());
        assertEquals(Set.of("endDate"), fieldsOfRecord(result, 1));
        assertEquals(1, scheduleRepo.size());
    }
}