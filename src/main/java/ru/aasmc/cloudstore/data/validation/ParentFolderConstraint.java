package ru.aasmc.cloudstore.data.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ParentFolderValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParentFolderConstraint {
    String message() default "Only folder can be a parent";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
