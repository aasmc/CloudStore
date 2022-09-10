package ru.aasmc.cloudstore.data.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SizeConstraintValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SizeConstraint {
    String message() default "Incorrect size";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
