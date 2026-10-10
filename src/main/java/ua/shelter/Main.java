package ua.shelter;
import ua.shelter.model.Animal;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;
import ua.shelter.model.CareSchedule;
import ua.shelter.util.ShelterUtils;
import java.time.LocalDate;

public class Main{
         public static void main(String[] args){
                  Species species= new Species("TYTYG5gg", "dog", "CARNIVORE"); 
                  System.out.println("Species: " + species);
                  Keeper keeper = new Keeper("Gor", "Gor Ruber", LocalDate.of(2010, 7, 23));
                  System.out.println("Keeper: " + keeper);
                  Animal animal= Animal.of("DFV46FH", "Bob", species , LocalDate.of(2016, 5, 23), "HEALTHY");
                  System.out.println("Animal: " + animal);
                  CareSchedule schedule = CareSchedule.of(animal, keeper, LocalDate.of(2018, 5, 23), null, 3);
                  System.out.println("CareSchedule: " + schedule);
                  String rawChip = "  dfv46fh ";
                  String normalizedChip = ShelterUtils.validateAndNormalizeChipNumber(rawChip);
                  System.out.println("Raw: '" + rawChip + "' -> Normalized: '" + normalizedChip + "'");

                  try{
                  ShelterUtils.dietIsAllow("PIZZA");
                  } catch (IllegalArgumentException e) {
                           System.out.println("Caught expected error: " + e.getMessage());
                  }
         
                  try {
                           ShelterUtils.validateFeedings(10);
                  } catch (IllegalArgumentException e) {
                           System.out.println("Caught expected error: " + e.getMessage());
                  }

                  try {
                           ShelterUtils.validateBirtheDate(LocalDate.now().plusDays(5));
                  } catch (IllegalArgumentException e) {
                           System.out.println("Caught expected error: " + e.getMessage());
                  }

                  try {
                           animal.setHealth("INVALID_HEALTH");
                  } catch (IllegalArgumentException e) {
                           System.out.println("Setter protection triggered: " + e.getMessage());
                  }

                  Animal animalTwin = Animal.of("DFV46FH", "Bob", species, LocalDate.of(2016, 5, 23), "HEALTHY");
                  Animal animalDifferent = Animal.of("DFV99999", "Max", species, LocalDate.of(2020, 1, 1), "HEALTHY");

                  System.out.println("animal == animalTwin: " + (animal == animalTwin)); 
                  System.out.println("animal.equals(animalTwin): " + animal.equals(animalTwin)); 
                  System.out.println("HashCode match: " + (animal.hashCode() == animalTwin.hashCode())); 
                  System.out.println("animal.equals(animalDifferent): " + animal.equals(animalDifferent)); 

                  long ageMonths = ShelterUtils.animalAgeMonths(animal);
                  System.out.println("Animal age in months: " + ageMonths);

                  long careDays = ShelterUtils.careDays(schedule);
                  System.out.println("Care active days: " + careDays);

                  System.out.println(species);
                  System.out.println(keeper);
                  System.out.println(animal);
                  System.out.println(schedule);

                  
                  /* he following line does not compile because ValidationHelper or its methods 
                  have package-private visibility and cannot be accessed from outside the util package
                  ua.shelter.util.ValidationHelper.requireNotNull(null, "test");
                  */
         }
}

