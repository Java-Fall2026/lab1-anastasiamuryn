package ua.shelter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.exception.DuplicateIdentityException;
import ua.shelter.model.Animal;
import ua.shelter.model.Keeper;
import ua.shelter.model.Species;
import ua.shelter.repository.GenericRepository;
import ua.shelter.util.ShelterReader;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("=== 1. Три репозиторії різних типів ===");
        GenericRepository<Species> speciesRepo = new GenericRepository<>("species", s -> s.getSpeciesCode());
        GenericRepository<Animal> animalRepo = new GenericRepository<>("animals", a -> a.getChipNumber());
        GenericRepository<Keeper> keeperRepo = new GenericRepository<>("keepers", k -> k.employeeBadge());

        Species dog = new Species("DOG001", "Dog", "CARNIVORE");
        Species cat = new Species("CAT002", "Cat", "CARNIVORE");
        speciesRepo.addAll(List.of(dog, cat));

        Animal bob = Animal.of("DFV46FH", "Bob", dog, LocalDate.of(2016, 5, 23), "HEALTHY");
        animalRepo.add(bob);
        keeperRepo.add(new Keeper("K100", "Gor Ruber", LocalDate.of(2010, 7, 23)));

        lookup(speciesRepo, "DOG001");   
        lookup(speciesRepo, "ZZZ999");   
        lookup(animalRepo, "DFV46FH");   
        lookup(animalRepo, "NOPE");      
        lookup(keeperRepo, "K100");      
        lookup(keeperRepo, "K999");      

        System.out.println("\n=== 2. Наповнення репозиторію з CSV ===");
        GenericRepository<Species> csvSpecies = new GenericRepository<>("csv-species", s -> s.getSpeciesCode());
        int rejected = 0;
        for (Species s : ShelterReader.loadSpecies("data/species.csv")) {
            try {
                csvSpecies.add(s);
            } catch (DuplicateIdentityException e) {
                rejected++;
                System.out.println("Дублікат з CSV пропущено: " + e.getMessage());
            }
        }
        System.out.println("У репозиторії видів з CSV: " + csvSpecies.size() + ", відхилено дублікатів: " + rejected);
        printAll("Усі види з CSV", csvSpecies.findAll());

        System.out.println("\n=== 3. Дублікат: add та addIfAbsent ===");
        Animal bobTwin = Animal.of("DFV46FH", "Bobby", dog, LocalDate.of(2016, 5, 23), "HEALTHY");
        try {
            animalRepo.add(bobTwin);
        } catch (DuplicateIdentityException e) {
            System.out.println("add: перехоплено виняток -> " + e.getMessage());
        }
        boolean added = animalRepo.addIfAbsent(bobTwin);
        System.out.println("addIfAbsent: повернув " + added + ", розмір репозиторію: " + animalRepo.size());

        System.out.println("\n=== 4. Видалення ===");
        System.out.println("Видалення наявного (K100): " + keeperRepo.removeByIdentity("K100"));
        System.out.println("Видалення відсутнього (K999): " + keeperRepo.removeByIdentity("K999"));

        System.out.println("\n=== 5. Логування: ERROR ===");
        try {
            animalRepo.add(null);
        } catch (IllegalArgumentException e) {
            System.out.println("add(null): перехоплено виняток -> " + e.getMessage());
        }
        System.out.println("(INFO -- успішні операції, WARN -- дублікати та ненайдене, ERROR -- null; див. лог вище)");
    }

    private static <T> void lookup(GenericRepository<T> repo, String identity) {
        Optional<T> found = repo.findByIdentity(identity);
        if (found.isPresent()) {
            System.out.println("Знайдено [" + identity + "]: " + found.get());
        } else {
            log.warn("Element '{}' not found", identity);
            System.out.println("Не знайдено [" + identity + "]");
        }
    }

       private static <T> void printAll(String title, Collection<? extends T> items) {
        System.out.println(title + " (" + items.size() + "):");
        for (T item : items) {
            System.out.println("  " + item);
        }
    }
}