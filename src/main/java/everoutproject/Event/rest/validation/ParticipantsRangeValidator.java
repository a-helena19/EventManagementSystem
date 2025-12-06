package everoutproject.Event.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class ParticipantsRangeValidator implements ConstraintValidator<ValidParticipantsRange, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            Integer minParticipants = getFieldValue(obj, "minParticipants");
            Integer maxParticipants = getFieldValue(obj, "maxParticipants");

            // If either is null, let @NotNull handle it
            if (minParticipants == null || maxParticipants == null) {
                return true;
            }

            boolean isValid = maxParticipants >= minParticipants;

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Max participants (" + maxParticipants + ") must be >= min participants (" + minParticipants + ")"
                ).addPropertyNode("maxParticipants").addConstraintViolation();
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }
    }

    private Integer getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Integer) field.get(obj);
    }
}
