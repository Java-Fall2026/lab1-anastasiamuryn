package ua.shelter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;

import java.util.Objects;

@JsonIgnoreProperties("createdAt")
public class Species extends BaseEntity {

         @NotBlank(message = "Код виду не може бути порожнім")
         @Size(min = 2, max = 20, message = "Код виду має містити від 2 до 20 символів")
         private String speciesCode;

         @NotBlank(message = "Назва виду не може бути порожньою")
         @Size(min = 2, max = 50, message = "Назва виду має містити від 2 до 50 символів")
         private String commonName;

         @NotBlank(message = "Тип харчування не може бути порожнім")
         @Pattern(regexp = "CARNIVORE|HERBIVORE|OMNIVORE",
                  message = "Тип харчування має бути CARNIVORE, HERBIVORE або OMNIVORE")
         private String diet;

         @JsonCreator
         public Species(@JsonProperty("speciesCode") String speciesCode,
                        @JsonProperty("commonName") String commonName,
                        @JsonProperty("diet") String diet) {
                  super();
                  this.speciesCode = ShelterUtils.normalize(speciesCode);
                  this.commonName = ShelterUtils.capitalize(commonName);
                  this.diet = ShelterUtils.normalize(diet);
         }

         public String getSpeciesCode() {
                  return speciesCode;
         }

         public String getCommonName() {
                  return commonName;
         }

         public String getDiet() {
                  return diet;
         }

         @Override
         public boolean equals(Object o){
                  if(this==o){
                           return true;
                  }
                  if(o==null || getClass()!=o.getClass()){
                           return false;
                  }
                  Species species = (Species)o;
                  return Objects.equals(speciesCode, species.speciesCode);
         }

         @Override
         public int hashCode(){
                  return Objects.hash(speciesCode);
         }

         @Override
         public String toString(){
                  return "Species{" + 
                  "speciesCode: " + speciesCode + '\'' +
                  ", commonName: " + commonName + '\'' +
                  ", diet: " + diet + '\'' +
                  '}';
         }

}