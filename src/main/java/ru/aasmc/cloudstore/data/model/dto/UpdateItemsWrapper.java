package ru.aasmc.cloudstore.data.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateItemsWrapper {
    private List<UpdateItemDto> items;
}
