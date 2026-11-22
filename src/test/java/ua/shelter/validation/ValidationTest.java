package ua.shelter.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final Species DOG = new Species("DOG001", "Dog", "CARNIVORE");
    private static final Keeper IVAN = new Keeper("K100", "Ivan Petrenko", LocalDate.of(2015, 3, 10));

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    private static Animal animal() {
        return Animal.of("CHIP001", "Bob", DOG, LocalDate.of(2020, 1, 1), "HEALTHY");
    }

    private static Set<String> paths(Object target) {
        return validator.validate(target).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static Set<String> messages(Object target) {
        return validator.validate(target).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    static Stream<Arguments> invalidObjects() {
        return Stream.of(
                Arguments.of("@NotBlank: blank nickname",
                        Animal.of("CHIP001", "   ", DOG, LocalDate.of(2020, 1, 1), "HEALTHY"), "nickname"),
                Arguments.of("@Size: too short nickname",
                        Animal.of("CHIP001", "A", DOG, LocalDate.of(2020, 1, 1), "HEALTHY"), "nickname"),
                Arguments.of("@NotNull: missing species",
                        Animal.of("CHIP001", "Bob", null, LocalDate.of(2020, 1, 1), "HEALTHY"), "species"),
                Arguments.of("@Pattern: unknown health value",
                        Animal.of("CHIP001", "Bob", DOG, LocalDate.of(2020, 1, 1), "SICK"), "health"),
                Arguments.of("@PastOrPresent: birth date in the future",
                        Animal.of("CHIP001", "Bob", DOG, TODAY.plusDays(1), "HEALTHY"), "birthDate"),
                Arguments.of("@Valid: nested species with wrong diet",
                        Animal.of("CHIP001", "Bob", new Species("DOG001", "Dog", "PLANTS"),
                                LocalDate.of(2020, 1, 1), "HEALTHY"), "species.diet"),
                Arguments.of("@Min: zero feedings per day",
                        CareSchedule.of(animal(), IVAN, LocalDate.of(2022, 1, 10), null, 0), "feedingsPerDay"),
                Arguments.of("@Max: too many feedings per day",
                        CareSchedule.of(animal(), IVAN, LocalDate.of(2022, 1, 10), null, 7), "feedingsPerDay"),
                Arguments.of("@PastOrPresent: hire date in the future",
                        new Keeper("K100", "Ivan Petrenko", TODAY.plusDays(1)), "hireDate")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidObjects")
    @DisplayName("Each standard annotation reports a violation on the expected field")
    void standardAnnotationsCatchInvalidValues(String label, Object invalid, String expectedPath) {
        Set<String> actual = paths(invalid);

        assertTrue(actual.contains(expectedPath),
                "Expected a violation on '" + expectedPath + "' but got " + actual);
    }

    @Test
    @DisplayName("A fully valid animal and a valid schedule have no violations")
    void validObjectsPass() {
        assertTrue(validator.validate(animal()).isEmpty());
        assertTrue(validator.validate(
                CareSchedule.of(animal(), IVAN, LocalDate.of(2022, 1, 10), LocalDate.of(2023, 1, 10), 3)).isEmpty());
    }

    @Test
    @DisplayName("All violations of one object are reported, not only the first one")
    void allViolationsAreReported() {
        Animal broken = Animal.of("CHIP001", "", DOG, TODAY.plusDays(1), "SICK");

        Set<String> actual = paths(broken);

        assertTrue(actual.containsAll(Set.of("nickname", "birthDate", "health")),
                "Expected violations on nickname, birthDate and health but got " + actual);
    }

    @Test
    @DisplayName("Validation messages are written in Ukrainian")
    void messagesAreInUkrainian() {
        Animal withoutSpecies = Animal.of("CHIP001", "Bob", null, LocalDate.of(2020, 1, 1), "HEALTHY");

        assertEquals(Set.of("Вид тварини обов'язковий"), messages(withoutSpecies));
    }

    @Test
    @DisplayName("Custom annotation: a valid period passes, including an open-ended and a same-day one")
    void customAnnotationAcceptsValidPeriods() {
        CareSchedule openEnded = CareSchedule.of(animal(), IVAN, LocalDate.of(2022, 1, 10), null, 3);
        CareSchedule sameDay = CareSchedule.of(animal(), IVAN, LocalDate.of(2022, 1, 10), LocalDate.of(2022, 1, 10), 3);

        assertTrue(validator.validate(openEnded).isEmpty());
        assertTrue(validator.validate(sameDay).isEmpty());
    }

    @Test
    @DisplayName("Custom annotation: end date before start date is a violation on endDate")
    void customAnnotationRejectsEndBeforeStart() {
        CareSchedule schedule = CareSchedule.of(animal(), IVAN, LocalDate.of(2022, 6, 1), LocalDate.of(2022, 1, 1), 2);

        assertEquals(Set.of("endDate"), paths(schedule));
    }

    @Test
    @DisplayName("Custom annotation: start date before the animal's birth date is a violation on startDate")
    void customAnnotationRejectsStartBeforeBirth() {
        CareSchedule schedule = CareSchedule.of(animal(), IVAN, LocalDate.of(2019, 12, 31), null, 2);

        assertEquals(Set.of("startDate"), paths(schedule));
    }

    @Test
    @DisplayName("Custom annotation: start date before the keeper's hire date is a violation on startDate")
    void customAnnotationRejectsStartBeforeHire() {
        Animal oldAnimal = Animal.of("CHIP009", "Rex", DOG, LocalDate.of(2010, 1, 1), "HEALTHY");
        CareSchedule schedule = CareSchedule.of(oldAnimal, IVAN, LocalDate.of(2014, 1, 1), null, 2);

        assertEquals(Set.of("startDate"), paths(schedule));
        assertTrue(messages(schedule).stream().anyMatch(m -> m.contains("прийому")));
    }
}