package ua.shelter.util;
import java.time.LocalDate;
import java.util.Set;

final class ValidationHelper {

         private ValidationHelper() {
         }

         public static void requireNotNull(Object obj, String message) {
                  if (obj == null) {
                           throw new NullPointerException(message);
                  }
         }

         public static void requireNotBlank(String val, String message) {
                  if (val == null || val.trim().isBlank()) {
                           throw new NullPointerException(message);
                  }
         }

         public static void requirePositive(double val, String message) {
                  if (val <= 0) {
                           throw new IllegalArgumentException(message);
                  }
         }

         public static void requireInRange(int value, int min, int max, String message) {
                  if (value < min || value > max) {
                           throw new IllegalArgumentException(message);
                  }
         }

         public static void requireDateInRange(LocalDate value, LocalDate min, LocalDate max, String message) {
                  if (value.isBefore(min) || value.isAfter(max)) {
                           throw new IllegalArgumentException(message);
                  }
         }

         public static void requireMoreThan(LocalDate dateBig, LocalDate dateSmall, String message) {
                  if (dateBig.isBefore(dateSmall)) {
                           throw new IllegalArgumentException(message);
                  }
         }

         public static void requireDateNotFuture(LocalDate date, String message) {
                  if (date.isAfter(LocalDate.now())) {
                           throw new IllegalArgumentException(message);
                  }
         }

         public static void requireDateNotPast(LocalDate date, String message) {
                  LocalDate min = LocalDate.of(2000, 01, 01);
                  if (date.isBefore(min)) {
                           throw new IllegalArgumentException(message);
                  }
         }

         public static void requireAllowValues(String val, Set<String> allowedValues, String message) {
                  requireNotBlank(val, message);
                  String upper = val.trim().toUpperCase();
                  if (!allowedValues.contains(upper)) {
                           throw new IllegalArgumentException(message);
                  }
         }

}