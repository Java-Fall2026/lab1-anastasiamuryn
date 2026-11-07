package ua.shelter.model;

import java.time.LocalDate;
import ua.shelter.util.ShelterUtils;

public record Keeper(String employeeBadge, String fullName, LocalDate hireDate) {
         public Keeper {
                  employeeBadge = ShelterUtils.validateAndNormalizeEmployeeBadge(employeeBadge);
                  fullName = ShelterUtils.validateAndCapitalizeFullName(fullName);
                  ShelterUtils.validateHireDate(hireDate);
         }
}