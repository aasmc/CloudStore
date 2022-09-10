package ru.aasmc.cloudstore.data.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemExtendedDto {

    private SystemItemDto systemItem;

    private List<SystemItemExtendedDto> children;

}
