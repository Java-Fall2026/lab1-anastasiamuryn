package ua.shelter.model;
import java.util.Objects;
import ua.shelter.util.ShelterUtils;
import ua.shelter.model.DietStatus;


public record Species(String speciesCode, String commonName, DietStatus diet) {
         public Species{
                  ShelterUtils.validateSpecies(speciesCode);
                  commonName = ShelterUtils.validateAndCapitalizeCommonName(commonName);
         }
}

         
