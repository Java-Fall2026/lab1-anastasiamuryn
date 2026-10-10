package ua.shelter.model;
import ua.common.BaseEntity;
import java.time.LocalDate;
import ua.shelter.util.ShelterUtils;

public class CareSchedule extends BaseEntity {
         private Animal animal;
         private Keeper keeper;
         private LocalDate startDate;
         private LocalDate endDate;
         private int feedingsPerDay;

         public static CareSchedule of(Animal animal, Keeper keeper, LocalDate startDate, LocalDate endDAte,
                           int feedingsPerDay) {
                  return new CareSchedule(animal, keeper, startDate, endDAte, feedingsPerDay);
         }

         private CareSchedule(Animal animal, Keeper keeper, LocalDate startDate, LocalDate endDate,
                           int feedingsPerDay) {
                  super();
                  ShelterUtils.validateAnimal(animal);
                  this.animal = animal;
                  ShelterUtils.validateKeeper(keeper);
                  this.keeper = keeper;
                  ShelterUtils.validateStartDate(startDate, animal.getBirthDate(), keeper. getHireDate());
                  this.startDate = startDate;
                  setEndDate(endDate);
                  setFeedingsPerDay(feedingsPerDay);
         }

         public Animal getAnimal() {
                  return animal;
         }

         public Keeper getKeeper() {
                  return keeper;
         }

         public LocalDate getStartDate() {
                  return startDate;
         }

         public LocalDate getEndDate() {
                  return endDate;
         }

         public final void setEndDate(LocalDate endDate) {
                  ShelterUtils.validateEndDate(endDate, this.startDate);
                  this.endDate = endDate;
         }

         public int getFeedingsPerDay() {
                  return feedingsPerDay;
         }

         public final void setFeedingsPerDay(int feedingsPerDay) {
                  ShelterUtils.validateFeedings(feedingsPerDay);
                  this.feedingsPerDay = feedingsPerDay;
         }
}