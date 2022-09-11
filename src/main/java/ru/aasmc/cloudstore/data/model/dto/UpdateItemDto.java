package ru.aasmc.cloudstore.data.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.aasmc.cloudstore.data.model.ItemType;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UpdateItemDto {
    @NotNull
    private String id;

    private String url;

    private String date;

    private String parentId;

    private ItemType type;

    private Integer size;
}
