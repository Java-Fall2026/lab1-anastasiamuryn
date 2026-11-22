package ua.shelter.util;

import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class ShelterUtils {

         private ShelterUtils() {
         }

         public static String normalize(String value) {
                  return value == null ? null : value.trim().toUpperCase();
         }

         public static String capitalize(String value) {
                  if (value == null) {
                           return null;
                  }
                  String trimmed = value.trim();
                  if (trimmed.isEmpty()) {
                           return trimmed;
                  }
                  return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1).toLowerCase();
         }

         public static long animalAgeMonths(Animal animal) {
                  Objects.requireNonNull(animal, "Animal must be not null");
                  Objects.requireNonNull(animal.getBirthDate(), "Birth date must be not null");
                  return ChronoUnit.MONTHS.between(animal.getBirthDate(), LocalDate.now());
         }

         public static long careDays(CareSchedule schedule) {
                  Objects.requireNonNull(schedule, "Schedule must be not null");
                  Objects.requireNonNull(schedule.getStartDate(), "Start date must be not null");
                  if (schedule.getEndDate() == null) {
                           return -1;
                  }
                  return ChronoUnit.DAYS.between(schedule.getStartDate(), schedule.getEndDate());
         }
}