package ru.aasmc.cloudstore.data.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.validation.SizeConstraint;
import ru.aasmc.cloudstore.data.validation.UrlConstraint;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@UrlConstraint
@SizeConstraint
@NoArgsConstructor
public class SystemItemDto {

    @NotNull
    private String id;

    private String url;

    private String parentId;

    private ItemType type;

    private Integer size;

}
