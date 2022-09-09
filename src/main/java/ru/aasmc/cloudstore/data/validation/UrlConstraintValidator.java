package ru.aasmc.cloudstore.data.validation;

import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.SystemItemDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrlConstraintValidator implements ConstraintValidator<UrlConstraint, SystemItemDto> {
    @Override
    public void initialize(UrlConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(SystemItemDto value, ConstraintValidatorContext context) {
        if (value.getType() == ItemType.FOLDER) {
            return value.getUrl() == null;
        } else {
            return value.getUrl() != null && value.getUrl().length() <= 255;
        }
    }
}
