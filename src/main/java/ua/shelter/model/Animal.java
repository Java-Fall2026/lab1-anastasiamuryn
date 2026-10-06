import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;
import java.time.LocalDate;

public class Animal extends BaseEntity {
         private String chipNumber;
         private String nickname;
         private Species species;
         private LocalDate birthDate;
         private String health;

         public static Animal of(String chipNumber, String nickname, Species species, LocalDate birthDate,
                           String health) {
                  return new Animal(chipNumber, nickname, species, birthDate, health);
         }

         private Animal(String chipNumber, String nickname, Species species, LocalDate birthDate, String health) {
                  super();
                  this.chipNumber = ShelterUtils.validateAndNormalizeChipNumber(chipNumber);
                  this.nickname = ShelterUtils.validateAndCapitalizeNickName(nickname);
                  ShelterUtils.validateSpecies(species);
                  this.species = species;
                  ShelterUtils.validateBirtheDate(birthDate);
                  this.birthDate = birthDate;
                  setHealth(health);
         }

         public String getChipNumber() {
                  return chipNumber;
         }

         public String getNickname() {
                  return nickname;
         }

         public Species getSpecies() {
                  return species;
         }

         public LocalDate getBirthDate() {
                  return birthDate;
         }

         public String getHealth() {
                  return health;
         }

         public final void setHealth(String health) {
                  this.health = ShelterUtils.healthIsAllow(health);
         }
}