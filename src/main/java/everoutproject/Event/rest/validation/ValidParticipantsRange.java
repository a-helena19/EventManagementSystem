package everoutproject.Event.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = ParticipantsRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidParticipantsRange {
    String message() default "Max participants must be greater than or equal to min participants";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
