package ua.shelter;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shelter.io.AppConfig;
import ua.shelter.io.DataLoader;
import ua.shelter.io.FileStorage;
import ua.shelter.io.LoadResult;
import ua.shelter.model.Animal;
import ua.shelter.model.CareSchedule;
import ua.shelter.model.Species;
import ua.shelter.repository.AnimalRepository;
import ua.shelter.repository.CareScheduleRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Application started");

        System.out.println("=== 1. Читання конфігурації ===");
        Path animalsFile;
        Path schedulesFile;
        Path brokenFile;
        Path outputFile;
        try {
            AppConfig config = AppConfig.fromClasspath();
            animalsFile = config.path("animals.file");
            schedulesFile = config.path("schedules.file");
            brokenFile = config.path("broken.file");
            outputFile = config.path("output.file");
        } catch (IOException | NoSuchElementException e) {
            log.error("Configuration error: {}", e.getMessage());
            System.out.println("Помилка конфігурації: " + e.getMessage());
            return;
        }
        log.info("Configuration loaded from app.properties");
        System.out.println("Файл тварин: " + animalsFile);
        System.out.println("Файл графіків догляду: " + schedulesFile);
        System.out.println("Пошкоджений файл: " + brokenFile);
        System.out.println("Файл для збереження: " + outputFile);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            run(factory.getValidator(), animalsFile, schedulesFile, brokenFile, outputFile);
        }
        log.info("Application finished");
    }

    private static void run(Validator validator, Path animalsFile, Path schedulesFile,
                            Path brokenFile, Path outputFile) {
        AnimalRepository animalRepo = new AnimalRepository();
        CareScheduleRepository scheduleRepo = new CareScheduleRepository();
        DataLoader<Animal> animalLoader = new DataLoader<>(Animal.class, animalRepo, validator);
        DataLoader<CareSchedule> scheduleLoader = new DataLoader<>(CareSchedule.class, scheduleRepo, validator);

        System.out.println("\n=== 2. Завантаження файлів даних ===");
        LoadResult animalsResult = animalLoader.load(animalsFile);
        printSummary(animalsFile, animalsResult);
        LoadResult schedulesResult = scheduleLoader.load(schedulesFile);
        printSummary(schedulesFile, schedulesResult);
        LoadResult total = animalsResult.merge(schedulesResult);
        System.out.println("Разом: прочитано " + total.read() + ", додано " + total.added()
                + ", відкинуто " + total.rejectedCount());
        System.out.println("У репозиторіях: тварин " + animalRepo.size()
                + ", графіків догляду " + scheduleRepo.size());

        System.out.println("\n=== 3. Відкинуті записи ===");
        printRejected(animalsResult);
        printRejected(schedulesResult);

        System.out.println("\n=== 4. Спроба завантажити пошкоджений файл ===");
        int before = animalRepo.size();
        LoadResult brokenResult = animalLoader.load(brokenFile);
        printSummary(brokenFile, brokenResult);
        printRejected(brokenResult);
        System.out.println("Тварин у репозиторії до: " + before + ", після: " + animalRepo.size()
                + " (програма продовжує роботу)");

        System.out.println("\n=== 5. Зміна репозиторію та збереження в новий файл ===");
        animalRepo.removeByIdentity("CHIP002");
        animalRepo.findByIdentity("CHIP001").ifPresent(animal -> animal.setHealth("QUARANTINE"));
        animalRepo.add(Animal.of("CHIP100", "Zeus", new Species("DOG001", "Dog", "CARNIVORE"),
                LocalDate.of(2025, 3, 3), "HEALTHY"));
        System.out.println("Видалено CHIP002, змінено здоров'я CHIP001, додано CHIP100");
        System.out.println("Тварин після змін: " + animalRepo.size());
        try {
            new FileStorage<>(Animal.class).save(outputFile, animalRepo.findAll());
            log.info("Saved {} animals to {}", animalRepo.size(), outputFile);
            System.out.println("Збережено у файл: " + outputFile);
        } catch (IOException e) {
            log.error("Failed to save {}: {}", outputFile, e.getMessage());
            System.out.println("Не вдалося зберегти файл: " + e.getMessage());
            return;
        }

        System.out.println("\n=== 6. Повторне завантаження збереженого файлу ===");
        AnimalRepository reloadedRepo = new AnimalRepository();
        LoadResult reloadedResult = new DataLoader<>(Animal.class, reloadedRepo, validator).load(outputFile);
        printSummary(outputFile, reloadedResult);
        List<String> original = animalRepo.findAll().stream().map(Animal::toString).toList();
        List<String> reloaded = reloadedRepo.findAll().stream().map(Animal::toString).toList();
        System.out.println("Вміст збігається: " + original.equals(reloaded));

        System.out.println("\n=== 7. Логування ===");
        System.out.println("(INFO -- підсумок по кожному файлу; WARN -- кожен відкинутий запис; "
                + "ERROR -- файл, який не вдалося прочитати; див. лог вище)");
    }

    private static void printSummary(Path file, LoadResult result) {
        System.out.println("Файл " + file.getFileName() + ": прочитано " + result.read()
                + ", додано " + result.added() + ", відкинуто " + result.rejectedCount());
    }

    private static void printRejected(LoadResult result) {
        if (result.rejected().isEmpty()) {
            System.out.println("Відкинутих записів немає");
            return;
        }
        result.rejected().forEach(rejection -> System.out.println("  " + rejection));
    }
}