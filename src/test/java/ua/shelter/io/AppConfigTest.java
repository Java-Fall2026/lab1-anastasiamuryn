package ua.shelter.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    @DisplayName("A missing key throws an exception whose message contains the key name")
    void missingKeyThrowsWithKeyName() {
        Properties properties = new Properties();
        properties.setProperty("animals.file", "data/animals.json");
        AppConfig config = new AppConfig(properties);

        NoSuchElementException ex =
                assertThrows(NoSuchElementException.class, () -> config.get("schedules.file"));

        assertTrue(ex.getMessage().contains("schedules.file"));
    }

    @Test
    @DisplayName("A blank value is treated as a missing key")
    void blankValueThrowsWithKeyName() {
        Properties properties = new Properties();
        properties.setProperty("animals.file", "   ");
        AppConfig config = new AppConfig(properties);

        NoSuchElementException ex =
                assertThrows(NoSuchElementException.class, () -> config.path("animals.file"));

        assertTrue(ex.getMessage().contains("animals.file"));
    }

    @Test
    @DisplayName("An existing key returns its value and a path")
    void existingKeyReturnsValue() {
        Properties properties = new Properties();
        properties.setProperty("animals.file", " data/animals.json ");
        AppConfig config = new AppConfig(properties);

        assertEquals("data/animals.json", config.get("animals.file"));
        assertEquals(Path.of("data/animals.json"), config.path("animals.file"));
    }

    @Test
    @DisplayName("The real app.properties contains all keys used by Main")
    void realConfigContainsAllKeys() throws IOException {
        AppConfig config = AppConfig.fromClasspath();

        assertEquals(Path.of("data/animals.json"), config.path("animals.file"));
        assertEquals(Path.of("data/schedules.yaml"), config.path("schedules.file"));
        assertEquals(Path.of("data/broken.json"), config.path("broken.file"));
        assertEquals(Path.of("data/animals_saved.json"), config.path("output.file"));
    }

    @Test
    @DisplayName("A missing configuration resource throws IOException with the resource name")
    void missingResourceThrowsIoException() {
        IOException ex = assertThrows(IOException.class, () -> AppConfig.fromClasspath("no-such.properties"));

        assertTrue(ex.getMessage().contains("no-such.properties"));
    }
}