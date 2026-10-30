package ua.shelter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.shelter.model.Species;
import ua.shelter.exception.InvalidDataException;
import ua.shelter.util.ShelterReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShelterTests {
    @Test
    @DisplayName("1. Verify correct Species object creation")
    void testSpeciesCreation_ValidData() {
        Species species = new Species("DOG001", "Dog", "CARNIVORE");
        assertNotNull(species, "Species object should be successfully created");
        assertEquals("Dog", species.getCommonName());
    }

    @Test
    @DisplayName("2. Verify InvalidDataException creation and message")
    void testInvalidDataException_Creation() {
        InvalidDataException ex = new InvalidDataException("Test data error");
        assertEquals("Test data error", ex.getMessage());
        assertNotNull(ex);
    }

    @Test
    @DisplayName("3. Load data from a valid temporary file")
    void testLoadSpecies_ValidFileWithTempDir(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test_species.csv");
        String content = "speciesCode,commonName,diet\nDOG001,Dog,CARNIVORE\nCAT002,Cat,CARNIVORE";
        Files.writeString(file, content);

        List<Species> result = ShelterReader.loadSpecies(file.toString());
        
        assertEquals(2, result.size(), "Should load exactly 2 records (ignoring header)");
        assertEquals("Dog", result.get(0).getCommonName(), "First loaded element should be Dog");
    }

    @Test
    @DisplayName("4. Handle empty file correctly")
    void testLoadSpecies_EmptyFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.csv");
        Files.writeString(file, "");

        List<Species> result = ShelterReader.loadSpecies(file.toString());
        assertTrue(result.isEmpty(), "List should be empty for an empty file");
    }

    @Test
    @DisplayName("5. Skip comments and blank lines in file")
    void testLoadSpecies_WithCommentsAndBlanks(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("comments.csv");
        String content = "speciesCode,commonName,diet\n\n# This is a comment\n   \nCAT002,Cat,CARNIVORE";
        Files.writeString(file, content);

        List<Species> result = ShelterReader.loadSpecies(file.toString());
        
        assertEquals(1, result.size(), "System should skip blank lines and comments, leaving 1 element");
    }

    @Test
    @DisplayName("6. Handle non-existent file gracefully")
    void testLoadSpecies_NonExistentFile() {
        List<Species> result = ShelterReader.loadSpecies("fake_path_99999.csv");
        
        assertNotNull(result, "Method should not return null");
        assertTrue(result.isEmpty(), "List should be empty when file is missing");
    }
}

