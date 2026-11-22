package ua.shelter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.shelter.model.CareSchedule;

import java.time.LocalDate;

public class CarePeriodValidator implements ConstraintValidator<ValidCarePeriod, CareSchedule> {

    @Override
    public boolean isValid(CareSchedule schedule, ConstraintValidatorContext context) {
        if (schedule == null || schedule.getStartDate() == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        LocalDate start = schedule.getStartDate();
        LocalDate end = schedule.getEndDate();

        if (end != null && end.isBefore(start)) {
            addViolation(context, "endDate", "Дата завершення догляду не може бути раніше дати початку");
            valid = false;
        }

        if (schedule.getAnimal() != null && schedule.getAnimal().getBirthDate() != null
                && start.isBefore(schedule.getAnimal().getBirthDate())) {
            addViolation(context, "startDate", "Дата початку догляду не може бути раніше дати народження тварини");
            valid = false;
        }

        if (schedule.getKeeper() != null && schedule.getKeeper().hireDate() != null
                && start.isBefore(schedule.getKeeper().hireDate())) {
            addViolation(context, "startDate", "Дата початку догляду не може бути раніше дати прийому працівника на роботу");
            valid = false;
        }

        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}