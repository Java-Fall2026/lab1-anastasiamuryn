package ua.shelter.model;
import ua.common.BaseEntity;
import java.util.Objects;
import ua.shelter.util.ShelterUtils;
import java.time.LocalDate;
import ua.shelter.model.HealthStatus;



public class Animal extends BaseEntity {
         private String chipNumber;
         private String nickname;
         private Species species;
         private LocalDate birthDate;
         private HealthStatus health;

         public static Animal of(String chipNumber, String nickname, Species species, LocalDate birthDate,
                           HealthStatus health) {
                  return new Animal(chipNumber, nickname, species, birthDate, health);
         }

         private Animal(String chipNumber, String nickname, Species species, LocalDate birthDate, HealthStatus health) {
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

         public HealthStatus getHealth() {
                  return health;
         }

         public final void setHealth(HealthStatus health) {
                  ShelterUtils.validateAnimalHealth(health);
                  this.health = health;
         }

         @Override
         public boolean equals(Object o){
                  if(this==o){
                           return true;
                  }
                  if(o==null || getClass()!=o.getClass()){
                           return false;
                  }
                  Animal animal = (Animal)o;
                  return Objects.equals(chipNumber, animal.chipNumber);
         }

         @Override
         public int hashCode(){
                  return Objects.hash(chipNumber);
         }

         @Override
         public String toString(){
                  return "Animal{" + 
                  "chipNumber : " + chipNumber + '\'' +
                  ", nickname: " + nickname + '\'' +
                  ", species : " + species + '\'' +
                  ", birthDate : " + birthDate + '\'' +
                  ", health : " + health + '\'' +
                  '}';
         }

}
