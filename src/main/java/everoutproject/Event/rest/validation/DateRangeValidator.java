package everoutproject.Event.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startDateField;
    private String endDateField;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            LocalDate startDate = getFieldValue(obj, startDateField);
            LocalDate endDate = getFieldValue(obj, endDateField);

            // If either is null, let @NotNull handle it
            if (startDate == null || endDate == null) {
                return true;
            }

            boolean isValid = !endDate.isBefore(startDate);

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "End date (" + endDate + ") must be after or equal to start date (" + startDate + ")"
                ).addPropertyNode(endDateField).addConstraintViolation();
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }
    }

    private LocalDate getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (LocalDate) field.get(obj);
    }
}
