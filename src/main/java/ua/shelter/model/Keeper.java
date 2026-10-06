import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;
import java.time.LocalDate;

public class Keeper extends BaseEntity {
         private String employeeBadge;
         private String fullName;
         private LocalDate hireDate;

         public Keeper(String employeeBadge, String fullName, LocalDate hireDate) {
                  super();
                  this.employeeBadge = ShelterUtils.validateAndNormalizeEmployeeBadge(employeeBadge);
                  this.fullName = ShelterUtils.validateAndCapitalizeFullName(fullName);
                  ShelterUtils.validateHireDate(hireDate);
                  this.hireDate = hireDate;
         }

         public String getEmployeeBadge() {
                  return employeeBadge;
         }

         public String getFullName() {
                  return fullName;
         }

         public LocalDate getHireDate() {
                  return hireDate;
         }

}