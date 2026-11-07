package ua.shelter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.shelter.exception.DuplicateIdentityException;
import ua.shelter.model.Animal;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericRepositoryTest {

    private static final Species DOG = new Species("DOG001", "Dog", "CARNIVORE");

    private GenericRepository<Animal> animals;

    @BeforeEach
    void setUp() {
        animals = new GenericRepository<>("animals", a -> a.getChipNumber());
    }

    private static Animal animal(String chip) {
        return Animal.of(chip, "Bob", DOG, LocalDate.of(2020, 1, 1), "HEALTHY");
    }

    @Test
    @DisplayName("add + findByIdentity finds the added item")
    void addAndFind() {
        Animal a = animal("CHIP1");
        animals.add(a);
        assertSame(a, animals.findByIdentity("CHIP1").orElseThrow());
    }

    @Test
    @DisplayName("findByIdentity on a missing identity returns an empty Optional")
    void findMissing() {
        assertTrue(animals.findByIdentity("NOPE").isEmpty());
    }

    @Test
    @DisplayName("second add with the same identity throws DuplicateIdentityException with details")
    void duplicateAddThrows() {
        animals.add(animal("CHIP1"));
        DuplicateIdentityException ex =
                assertThrows(DuplicateIdentityException.class, () -> animals.add(animal("CHIP1")));
        assertTrue(ex.getMessage().contains("CHIP1"));
        assertTrue(ex.getMessage().contains("animals"));
    }

    @Test
    @DisplayName("addIfAbsent returns false on a duplicate and keeps size unchanged")
    void addIfAbsentDuplicate() {
        assertTrue(animals.addIfAbsent(animal("CHIP1")));
        assertFalse(animals.addIfAbsent(animal("CHIP1")));
        assertEquals(1, animals.size());
    }

    @Test
    @DisplayName("removeByIdentity removes an existing item")
    void removeExisting() {
        animals.add(animal("CHIP1"));
        assertTrue(animals.removeByIdentity("CHIP1"));
        assertEquals(0, animals.size());
    }

    @Test
    @DisplayName("removeByIdentity returns false for a missing item")
    void removeMissing() {
        animals.add(animal("CHIP1"));
        assertFalse(animals.removeByIdentity("NOPE"));
        assertEquals(1, animals.size());
    }

    @Test
    @DisplayName("modifying the result of findAll does not change the repository")
    void findAllIsSafeSnapshot() {
        animals.add(animal("CHIP1"));
        List<Animal> all = animals.findAll();
        assertThrows(UnsupportedOperationException.class, all::clear);
        assertEquals(1, animals.size());
    }

    @Test
    @DisplayName("the same repository class works with a class, a mutable class and a record")
    void worksWithDifferentTypes() {
        GenericRepository<Keeper> keepers = new GenericRepository<>("keepers", k -> k.employeeBadge());
        GenericRepository<Species> species = new GenericRepository<>("species", s -> s.getSpeciesCode());

        keepers.add(new Keeper("K1", "Ivan Petrenko", LocalDate.of(2015, 1, 1)));
        species.add(DOG);
        animals.add(animal("CHIP1"));

        assertTrue(keepers.findByIdentity("K1").isPresent());
        assertTrue(species.findByIdentity("DOG001").isPresent());
        assertTrue(animals.findByIdentity("CHIP1").isPresent());
    }

    @Test
    @DisplayName("add(null) and a blank identity throw IllegalArgumentException")
    void rejectsNullAndBlankIdentity() {
        assertThrows(IllegalArgumentException.class, () -> animals.add(null));

        GenericRepository<String> strings = new GenericRepository<>("strings", s -> s);
        assertThrows(IllegalArgumentException.class, () -> strings.add("   "));
        assertEquals(0, strings.size());
    }

    @Test
    @DisplayName("addAll accepts a collection of a subtype thanks to the wildcard")
    void addAllAcceptsSubtypes() {
        GenericRepository<Number> numbers = new GenericRepository<>("numbers", n -> n.toString());
        List<Integer> ints = List.of(1, 2, 3);
        numbers.addAll(ints);
        assertEquals(3, numbers.size());
    }
}