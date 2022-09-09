package ru.aasmc.cloudstore.data.validation;

import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.SystemItemDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SizeConstraintValidator implements ConstraintValidator<SizeConstraint, SystemItemDto> {
    @Override
    public void initialize(SizeConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(SystemItemDto value, ConstraintValidatorContext context) {
        if (value.getType() == ItemType.FOLDER) {
            return value.getSize() == null;
        } else {
            return value.getSize() > 0;
        }
    }
}
