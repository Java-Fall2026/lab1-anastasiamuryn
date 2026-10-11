package ua.shelter.model;
import java.util.Objects;
import ua.shelter.util.ShelterUtils;


public record Species(String speciesCode, String commonName, DietStatus diet) {
         public Species{
                  ShelterUtils.validateSpecies(speciesCode);
                  commonName = ShelterUtils.validateAndCapitalizeCommonName(commonName);
         }
}

         
