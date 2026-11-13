package ua.shelter.model;

import ua.common.BaseEntity;
import ua.shelter.util.ShelterUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

public class CareSchedule extends BaseEntity implements Comparable<CareSchedule> {

    private static final Comparator<CareSchedule> NATURAL_ORDER =
            Comparator.comparing(CareSchedule::getStartDate)
                    .thenComparing(CareSchedule::getAnimal);

    private Animal animal;
    private Keeper keeper;
    private LocalDate startDate;
    private LocalDate endDate;
    private int feedingsPerDay;

    public static CareSchedule of(Animal animal, Keeper keeper, LocalDate startDate, LocalDate endDAte,
                                  int feedingsPerDay) {
        return new CareSchedule(animal, keeper, startDate, endDAte, feedingsPerDay);
    }

    public CareSchedule(Animal animal, Keeper keeper, LocalDate startDate, LocalDate endDate,
                        int feedingsPerDay) {
        super();
        ShelterUtils.validateAnimal(animal);
        this.animal = animal;
        ShelterUtils.validateKeeper(keeper);
        this.keeper = keeper;
        ShelterUtils.validateStartDate(startDate, animal.getBirthDate(), keeper.hireDate());
        this.startDate = startDate;
        setEndDate(endDate);
        setFeedingsPerDay(feedingsPerDay);
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
        ShelterUtils.validateEndDate(endDate, this.startDate);
        this.endDate = endDate;
    }

    public int getFeedingsPerDay() {
        return feedingsPerDay;
    }

    public final void setFeedingsPerDay(int feedingsPerDay) {
        ShelterUtils.validateFeedings(feedingsPerDay);
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