import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;

public class Species extends BaseEntity {
         private String speciesCode;
         private String commonName;
         private String diet;

         public Species(String speciesCode, String commonName, String diet) {
                  super();
                  this.speciesCode = ShelterUtils.validateAndNormalizeSpeciesCode(speciesCode);
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