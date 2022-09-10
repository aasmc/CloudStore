package ru.aasmc.cloudstore.data.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.aasmc.cloudstore.data.validation.DateConstraint;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ImportsDto {

    List<SystemItemDto> items;

    @DateConstraint(groups = BasicValidation.class)
    String updateDate;
}
