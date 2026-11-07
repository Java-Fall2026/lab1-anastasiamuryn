package ua.shelter;
import ua.shelter.model.Animal;
import ua.shelter.model.Species;
import ua.shelter.util.ShelterReader;
import java.time.LocalDate;
import java.util.List;

public class Main{
         public static void main(String[] args){

                  try {
                           java.nio.file.Path tempMixedFile = java.nio.file.Path.of("data/mixed_test.csv");
                           java.nio.file.Files.writeString(tempMixedFile, "speciesCode,commonName,diet\nDOG001,Dog,CARNIVORE\nBAD_ROW_WITHOUT_PROPER_DATA");
                           ShelterReader.loadSpecies(tempMixedFile.toString());
                  } catch (Exception e) {
                           System.out.println("Помилка: " + e.getMessage());
                  }
                  System.out.println();

                  List<Species> emptyResult = ShelterReader.loadSpecies("data/non_existent.csv");
                  System.out.println("Результат (очікується порожній список): " + emptyResult.size());
                  
                  List<Species> speciesList = ShelterReader.loadSpecies("data/species.csv");
                  System.out.println("Успішно завантажено видів: " + speciesList.size());
                  for (Species s : speciesList) {
                           System.out.println("   [LOADED] " + s);
                  }

                  if (!speciesList.isEmpty()) {
                           Species sampleSpecies = speciesList.get(0);
                           Animal animal = Animal.of("CHIP123", "Барсік", sampleSpecies, LocalDate.of(2020, 1, 1), "HEALTHY");
                           System.out.println("Початковий об'єкт тварини: " + animal);
                           try {
                                    System.out.println("Спроба передати некоректне значення у метод setHealth()...");
                                    animal.setHealth("BAD_HEALTH_STATUS");
                           } catch (IllegalArgumentException e) {
                                    System.out.println("Перехоплено очікуваний виняток від сеттера: " + e.getMessage());
                           }
                  }
         }
}

