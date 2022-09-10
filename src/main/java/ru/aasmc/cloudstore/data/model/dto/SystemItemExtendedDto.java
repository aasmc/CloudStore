package ru.aasmc.cloudstore.data.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.aasmc.cloudstore.data.model.ItemType;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemExtendedDto {

    @NotNull(groups = BasicValidation.class)
    private String id;

    private String url;

    private ItemType type;

    private String parentId;

    private String date;

    private Integer size;

    private List<SystemItemExtendedDto> children;

}
