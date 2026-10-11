package ua.shelter.model;

public enum HealthStatus {
         HEALTHY("Здоровий"),
         UNDER_TREATMENT("У процесі лікування"),
         QUARANTINE("У карантині");

         private final String health;

         HealthStatus(String health){
                  this.health=health;
         }

         public String getHealth() {
                  return health
         }
}

