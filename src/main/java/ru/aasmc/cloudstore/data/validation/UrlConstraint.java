package ru.aasmc.cloudstore.data.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UrlConstraintValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlConstraint {
    String message() default "Only files can have urls";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
