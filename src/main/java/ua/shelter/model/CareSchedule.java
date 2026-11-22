package ua.shelter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ua.common.BaseEntity;
import ua.shelter.validation.ValidCarePeriod;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

@ValidCarePeriod
@JsonIgnoreProperties("createdAt")
public class CareSchedule extends BaseEntity implements Comparable<CareSchedule> {

    private static final Comparator<CareSchedule> NATURAL_ORDER =
            Comparator.comparing(CareSchedule::getStartDate)
                    .thenComparing(CareSchedule::getAnimal);

    @NotNull(message = "Тварина обов'язкова")
    @Valid
    private Animal animal;

    @NotNull(message = "Працівник обов'язковий")
    @Valid
    private Keeper keeper;

    @NotNull(message = "Дата початку догляду обов'язкова")
    private LocalDate startDate;

    private LocalDate endDate;

    @Min(value = 1, message = "Кількість годувань на день має бути не менше 1")
    @Max(value = 6, message = "Кількість годувань на день має бути не більше 6")
    private int feedingsPerDay;

    public static CareSchedule of(Animal animal, Keeper keeper, LocalDate startDate, LocalDate endDAte,
                                  int feedingsPerDay) {
        return new CareSchedule(animal, keeper, startDate, endDAte, feedingsPerDay);
    }

    @JsonCreator
    public CareSchedule(@JsonProperty("animal") Animal animal,
                        @JsonProperty("keeper") Keeper keeper,
                        @JsonProperty("startDate") LocalDate startDate,
                        @JsonProperty("endDate") LocalDate endDate,
                        @JsonProperty("feedingsPerDay") int feedingsPerDay) {
        super();
        this.animal = animal;
        this.keeper = keeper;
        this.startDate = startDate;
        this.endDate = endDate;
        this.feedingsPerDay = feedingsPerDay;
    }

    public Animal getAnimal() {
        return animal;
    }

    public Keeper getKeeper() {
        return keeper;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public final void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getFeedingsPerDay() {
        return feedingsPerDay;
    }

    public final void setFeedingsPerDay(int feedingsPerDay) {
        this.feedingsPerDay = feedingsPerDay;
    }

    @Override
    public int compareTo(CareSchedule other) {
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
        CareSchedule schedule = (CareSchedule) o;
        return Objects.equals(animal, schedule.animal) && Objects.equals(startDate, schedule.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animal, startDate);
    }

    @Override
    public String toString() {
        return "CareSchedule{" +
                "animal : " + animal + '\'' +
                ", keeper : " + keeper + '\'' +
                ", startDate : " + startDate + '\'' +
                ", endDate : " + endDate + '\'' +
                ", feedingsPerDay : " + feedingsPerDay + '\'' +
                '}';
    }
}