package ua.shelter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

@JsonIgnoreProperties("createdAt")
public class Animal extends BaseEntity implements Comparable<Animal> {

    private static final Comparator<Animal> NATURAL_ORDER =
            Comparator.comparing(Animal::getNickname)
                    .thenComparing(Animal::getChipNumber);

    @NotBlank(message = "Номер чіпа не може бути порожнім")
    @Size(min = 3, max = 20, message = "Номер чіпа має містити від 3 до 20 символів")
    private String chipNumber;

    @NotBlank(message = "Кличка не може бути порожньою")
    @Size(min = 2, max = 30, message = "Кличка має містити від 2 до 30 символів")
    private String nickname;

    @NotNull(message = "Вид тварини обов'язковий")
    @Valid
    private Species species;

    @NotNull(message = "Дата народження обов'язкова")
    @PastOrPresent(message = "Дата народження не може бути в майбутньому")
    private LocalDate birthDate;

    @NotBlank(message = "Стан здоров'я не може бути порожнім")
    @Pattern(regexp = "HEALTHY|UNDER_TREATMENT|QUARANTINE",
             message = "Стан здоров'я має бути HEALTHY, UNDER_TREATMENT або QUARANTINE")
    private String health;

    @JsonCreator
    public static Animal of(@JsonProperty("chipNumber") String chipNumber,
                            @JsonProperty("nickname") String nickname,
                            @JsonProperty("species") Species species,
                            @JsonProperty("birthDate") LocalDate birthDate,
                            @JsonProperty("health") String health) {
        return new Animal(chipNumber, nickname, species, birthDate, health);
    }

    private Animal(String chipNumber, String nickname, Species species, LocalDate birthDate, String health) {
        super();
        this.chipNumber = ShelterUtils.normalize(chipNumber);
        this.nickname = ShelterUtils.capitalize(nickname);
        this.species = species;
        this.birthDate = birthDate;
        setHealth(health);
    }

    public String getChipNumber() {
        return chipNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public Species getSpecies() {
        return species;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getHealth() {
        return health;
    }

    public final void setHealth(String health) {
        this.health = ShelterUtils.normalize(health);
    }

    @Override
    public int compareTo(Animal other) {
        return NATURAL_ORDER.compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Animal animal = (Animal) o;
        return Objects.equals(chipNumber, animal.chipNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chipNumber);
    }

    @Override
    public String toString() {
        return "Animal{" +
                "chipNumber : " + chipNumber + '\'' +
                ", nickname: " + nickname + '\'' +
                ", species : " + species + '\'' +
                ", birthDate : " + birthDate + '\'' +
                ", health : " + health + '\'' +
                '}';
    }
}