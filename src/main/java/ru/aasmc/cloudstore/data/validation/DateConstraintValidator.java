package ru.aasmc.cloudstore.data.validation;

import ru.aasmc.cloudstore.data.model.SystemItemDto;
import ru.aasmc.cloudstore.util.DateProcessor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateConstraintValidator implements ConstraintValidator<DateConstraint, SystemItemDto> {
    @Override
    public void initialize(DateConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(SystemItemDto value, ConstraintValidatorContext context) {
        try {
            DateProcessor.toDate(value.getModifiedAt());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
