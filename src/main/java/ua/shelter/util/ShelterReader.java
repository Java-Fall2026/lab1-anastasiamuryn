package ua.shelter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.exception.InvalidDataException;
import ua.shelter.model.Species;


import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShelterReader {
         private static final Logger logger = LoggerFactory.getLogger(ShelterReader.class);

         public static List<Species> loadSpecies(String filePath) {
         List<Species> speciesList = new ArrayList<>();
         int n = 0;
         try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
                  String line;
                  boolean isHeader = true;

                  while ((line = reader.readLine()) != null) {
                           n++;
                           if (n==1) {
                                    continue; 
                           }
                           if (line.isBlank() || line.startsWith("#")) {
                           continue; 
                           }
                           try {
                                    String[] parts = line.split(",");
                                    if (parts.length != 3) {
                                            throw new InvalidDataException("Row " + n + ": Expected 3 columns, but got " + parts.length);
                                    }
                                    Species species = new Species(parts[0].trim(), parts[1].trim(), parts[2].trim());
                                    speciesList.add(species);
                           } catch (InvalidDataException | IllegalArgumentException e) {
                           logger.warn("Error in row {}: {}", n, e.getMessage());
                           }
                  }
         } catch (IOException e) {
                  logger.error("Failed to read file (file missing or inaccessible): {}", e.getMessage());
                  logger.info("Reading file {} completed.", filePath);
         }
         return speciesList;
}
}
