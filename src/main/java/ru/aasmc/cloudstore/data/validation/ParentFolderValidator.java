package ru.aasmc.cloudstore.data.validation;

import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.SystemItemDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ParentFolderValidator implements ConstraintValidator<ParentFolderConstraint, SystemItemDto> {
    @Override
    public void initialize(ParentFolderConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(SystemItemDto value, ConstraintValidatorContext context) {
        return value.getType() == ItemType.FOLDER;
    }
}
