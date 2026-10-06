import ua.shelter.model.CareSchedule;
import ua.shelter.model.Animal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import ua.shelter.model.Keeper;

public final class ShelterUtils {
         private static final Set<String> ALLOWED_DIETS = Set.of("CARNIVORE", "HERBIVORE", "OMNIVORE");
         private static final Set<String> ALLOWED_HEALTH = Set.of("HEALTHY", "UNDER_TREATMENT", "QUARANTINE");

         private ShelterUtils() {
         }

         public static void validateAnimal(Object obj) {
                  String message = "Animal must be not blank, got: " + obj;
                  FormatHelper.requireNotNull(obj, message);
         }

         public static void validateKeeper(Object obj) {
                  String message = "Keeper must be not blank, got: " + obj;
                  FormatHelper.requireNotNull(obj, message);
         }

         public static void validateSpecies(String value) {
                  String message = " Species must be not blank, got: " + value;
                  FormatHelper.requireNotNull(value, message);
         }

         public static String validateAndNormalizeChipNumber(String value) {
                  String message = "Chip Number must be not blank, got: " + value;
                  return FormatHelper.normalize(value, message);
         }

         public static String validateAndNormalizeEmployeeBadge(String value) {
                  String message = "EmployeeBadge must be not blank, got: " + value;
                  return FormatHelper.normalize(value, message);
         }

         public static String validateAndCapitalizeCommonName(String value) {
                  String message = "Common name must be not blank, got: " + value;
                  return FormatHelper.capitalize(value, message);
         }

         public static String validateAndCapitalizeFullName(String value) {
                  String message = "Full name must be not blank, got: " + value;
                  return FormatHelper.capitalize(value, message);
         }

         public static String validateAndCapitalizeNickName(String value) {
                  String message = "Nick name must be not blank, got: " + value;
                  return FormatHelper.capitalize(value, message);
         }

         public static String dietIsAllow(String value) {
                  String message = " Diet must be not blank, got: " + value;
                  String upperValue = FormatHelper.normalize(value);
                  String message2 = "Diet must be CARNIVORE or HERBIVORE or OMNIVORE, got: " + value;
                  ValidationHelper.requireAllowValues(upperValue, ALLOWED_DIETS, message2);
                  return upperValue;
         }

         public static String healthIsAllow(String value) {
                  String message = " Health must be not blank, got: " + value;
                  String message2 = " Health must be HEALTHY or UNDER_TREATMENT or QUARANTINE, got: " + value;
                  String upperValue = FormatHelper.normalize(value);
                  ValidationHelper.requireAllowValues(upperValue, ALLOWED_HEALTH, message2);
                  return upperValue;
                  ;
         }

         public static void validateBirtheDate(LocalDate date) {
                  String message = "Birth date can't be in future, got: " + date;
                  ValidationHelper.requireDateNotFuture(date, message);
         }

         public static void validateHireDate(LocalDate date) {
                  String message = "Hire date can't be in future, got: " + date;
                  ValidationHelper.requireDateNotPast(date, message);
         }

         public static void validateStartDate(LocalDate dateStart, LocalDate dateBirth, LocalDate dateHire) {
                  String message = "Start date can't be o before birht date, got: " + dateStart;
                  String message2 = "Start date can't be o before hire date, got: " + dateStart;
                  ValidationHelper.requireMoreThan(dateStart, dateBirth, message);
                  ValidationHelper.requireMoreThan(dateStart, dateHire, message2);
         }

         public static void validateEndDate(LocalDate dateEnd, LocalDate dateStart) {
                  String message = "End date can't be o before birht date, got: " + dateEnd;
                  if (dateEnd != null) {
                           ValidationHelper.requireMoreThan(dateEnd, dateStart, message);
                  }
         }

         public static void validateFeedings(int n) {
                  String message = "Count of feedings per day must be in range 1 to 6, got: " + n;
                  ValidationHelper.requireInRange(n, 1, 6, message);
         }

         public static long animalAgeMonths(Animal animal) {
                  String message1 = "Birh date must be not null, got: " + animal.getBirthDate();
                  String message = "Animal must be not null";
                  ValidationHelper.requireNotNull(animal, message);
                  ValidationHelper.requireNotNull(animal.getBirthDate(), message);
                  return ChronoUnit.MONTHS.between(animal.getBirthDate(), LocalDate.now());
         }

         public static long careDays(CareSchedule schedule) {
                  String message = "Schedule must be not null";
                  ValidationHelper.requireNotNull(schedule, message);
                  ValidationHelper.requireNotNull(schedule.getStartDate(), message);
                  if (schedule.getEndDate() == null) {
                           return -1;
                  }
                  return ChronoUnit.DAYS.between(schedule.getStartDate(), schedule.getEndDate());
         }
}