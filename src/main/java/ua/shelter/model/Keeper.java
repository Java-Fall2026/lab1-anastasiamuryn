package ua.shelter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import ua.shelter.util.ShelterUtils;

import java.time.LocalDate;

public record Keeper(
         @NotBlank(message = "Бейдж працівника не може бути порожнім")
         @Size(min = 2, max = 20, message = "Бейдж працівника має містити від 2 до 20 символів")
         String employeeBadge,

         @NotBlank(message = "Повне ім'я працівника не може бути порожнім")
         @Size(min = 3, max = 100, message = "Повне ім'я працівника має містити від 3 до 100 символів")
         String fullName,

         @NotNull(message = "Дата прийому на роботу обов'язкова")
         @PastOrPresent(message = "Дата прийому на роботу не може бути в майбутньому")
         LocalDate hireDate) {

         @JsonCreator
         public Keeper(@JsonProperty("employeeBadge") String employeeBadge,
                       @JsonProperty("fullName") String fullName,
                       @JsonProperty("hireDate") LocalDate hireDate) {
                  this.employeeBadge = ShelterUtils.normalize(employeeBadge);
                  this.fullName = ShelterUtils.capitalize(fullName);
                  this.hireDate = hireDate;
         }
}