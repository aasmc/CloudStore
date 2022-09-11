package ru.aasmc.cloudstore.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "mod_item")
@NoArgsConstructor
@Setter
@Getter
public class ModItem {

    @EmbeddedId
    private ModItemId id;
}
