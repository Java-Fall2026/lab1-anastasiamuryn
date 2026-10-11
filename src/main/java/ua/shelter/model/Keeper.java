package ua.shelter.model;
import java.util.Objects;
import ua.shelter.util.ShelterUtils;
import java.time.LocalDate;

public record Keeper(String employeeBadge, String fullName, LocalDate hireDate) {
         public Keeper{
                  employeeBadge = ShelterUtils.validateAndNormalizeEmployeeBadge(employeeBadge);
                  fullName = ShelterUtils.validateAndCapitalizeFullName(fullName);
                  ShelterUtils.validateHireDate(hireDate);
         }

}
