package ua.shelter.model;
import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;

public class Species extends BaseEntity {
         private String speciesCode;
         private String commonName;
         private String diet;

         public Species(String speciesCode, String commonName, String diet) {
                  super();
                  ShelterUtils.validateSpecies(speciesCode);
                  this.speciesCode = speciesCode;
                  this.commonName = ShelterUtils.validateAndCapitalizeCommonName(commonName);
                  this.diet = ShelterUtils.dietIsAllow(diet);
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

}