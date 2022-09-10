package ru.aasmc.cloudstore.data.validation;

import ru.aasmc.cloudstore.util.DateProcessor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateConstraintValidator implements ConstraintValidator<DateConstraint, String> {
    @Override
    public void initialize(DateConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return DateProcessor.checkDate(value);
    }
}
