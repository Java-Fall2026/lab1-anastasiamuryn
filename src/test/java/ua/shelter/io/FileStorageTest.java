package ua.shelter.io;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    private static final Species DOG = new Species("DOG001", "Dog", "CARNIVORE");
    private static final Species CAT = new Species("CAT002", "Cat", "CARNIVORE");
    private static final Keeper IVAN = new Keeper("K100", "Ivan Petrenko", LocalDate.of(2015, 3, 10));

    private static List<Animal> sampleAnimals() {
        return List.of(
                Animal.of("CHIP001", "Bob", DOG, LocalDate.of(2020, 1, 1), "HEALTHY"),
                Animal.of("CHIP002", "Misty", CAT, LocalDate.of(2021, 7, 1), "QUARANTINE"));
    }

    private static List<CareSchedule> sampleSchedules() {
        List<Animal> animals = sampleAnimals();
        return List.of(
                CareSchedule.of(animals.get(0), IVAN, LocalDate.of(2022, 1, 10), null, 3),
                CareSchedule.of(animals.get(1), IVAN, LocalDate.of(2022, 5, 15), LocalDate.of(2023, 5, 15), 4));
    }

    private static List<String> asText(List<?> items) {
        return items.stream().map(Object::toString).toList();
    }

    @Test
    @DisplayName("JSON: save and then load returns an equal list of animals")
    void jsonRoundTrip(@TempDir Path tempDir) throws IOException {
        FileStorage<Animal> storage = new FileStorage<>(Animal.class);
        Path file = tempDir.resolve("animals.json");
        List<Animal> original = sampleAnimals();

        storage.save(file, original);
        List<Animal> loaded = storage.load(file);

        assertEquals(original, loaded);
        assertEquals(asText(original), asText(loaded));
    }

    @Test
    @DisplayName("YAML: save and then load returns an equal list of care schedules")
    void yamlRoundTrip(@TempDir Path tempDir) throws IOException {
        FileStorage<CareSchedule> storage = new FileStorage<>(CareSchedule.class);
        Path file = tempDir.resolve("schedules.yaml");
        List<CareSchedule> original = sampleSchedules();

        storage.save(file, original);
        List<CareSchedule> loaded = storage.load(file);

        assertEquals(original, loaded);
        assertEquals(asText(original), asText(loaded));
    }

    @Test
    @DisplayName("The .yml extension is handled the same way as .yaml")
    void ymlExtensionWorks(@TempDir Path tempDir) throws IOException {
        FileStorage<Animal> storage = new FileStorage<>(Animal.class);
        Path file = tempDir.resolve("animals.yml");
        List<Animal> original = sampleAnimals();

        storage.save(file, original);

        assertEquals(asText(original), asText(storage.load(file)));
    }

    @ParameterizedTest(name = "dates are written as ISO strings in {0}")
    @ValueSource(strings = {"animals.json", "animals.yaml"})
    @DisplayName("Dates in the saved file are ISO strings, not number arrays")
    void datesAreIsoStrings(String fileName, @TempDir Path tempDir) throws IOException {
        FileStorage<Animal> storage = new FileStorage<>(Animal.class);
        Path file = tempDir.resolve(fileName);

        storage.save(file, sampleAnimals());

        JsonNode root = DataFormat.fromPath(file).createMapper().readTree(file.toFile());
        JsonNode birthDate = root.get(0).get("birthDate");
        assertTrue(birthDate.isTextual(), "birthDate must be a string");
        assertEquals("2020-01-01", birthDate.asText());
    }

    @Test
    @DisplayName("Unknown file extension throws an exception on save and on load")
    void unknownExtensionThrows(@TempDir Path tempDir) {
        FileStorage<Animal> storage = new FileStorage<>(Animal.class);
        Path csv = tempDir.resolve("animals.csv");
        Path noExtension = tempDir.resolve("animals");

        IllegalArgumentException onSave =
                assertThrows(IllegalArgumentException.class, () -> storage.save(csv, sampleAnimals()));
        assertThrows(IllegalArgumentException.class, () -> storage.load(csv));
        assertThrows(IllegalArgumentException.class, () -> storage.load(noExtension));
        assertTrue(onSave.getMessage().contains("csv"));
    }
}