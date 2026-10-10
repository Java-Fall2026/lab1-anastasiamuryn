package ua.shelter.model;
import ua.common.BaseEntity;
import java.util.Objects;
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

         @Override
         public boolean equals(Object o){
                  if(this==o){
                           return true;
                  }
                  if(o==null || getClass()!=o.getClass()){
                           return false;
                  }
                  Keeper keeper = (Keeper)o;
                  return Objects.equals(employeeBadge, keeper.employeeBadge);
         }

         @Override
         public int hashCode(){
                  return Objects.hash(employeeBadge);
         }

         @Override
         public String toString(){
                  return "Keeper{" + 
                  "employeeBadge: " + employeeBadge + '\'' +
                  ", fullName: " + fullName + '\'' +
                  ", hireDate: " + hireDate + '\'' +
                  '}';
         }

}
