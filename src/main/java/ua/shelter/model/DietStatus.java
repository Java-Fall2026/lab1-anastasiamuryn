package ua.shelter.model;

public enum DietStatus {
         CARNIVORE("Хижак"),
         HERBIVORE("Тровоїдний"),
         OMNIVORE("Всеїдний");

         private final String diet;

         DietStatus(String diet){
                  this.diet=diet;
         }

         public String getDiet(){
                  return diet;
         }
}
