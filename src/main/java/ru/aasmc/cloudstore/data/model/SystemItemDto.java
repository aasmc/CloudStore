package ru.aasmc.cloudstore.data.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.aasmc.cloudstore.data.validation.DateConstraint;
import ru.aasmc.cloudstore.data.validation.ParentFolderConstraint;
import ru.aasmc.cloudstore.data.validation.SizeConstraint;
import ru.aasmc.cloudstore.data.validation.UrlConstraint;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity(name = "system_item_dto")
@ToString
public class SystemItemDto {

    @Id
    @Column(nullable = false)
    @NotNull
    private String id;

    @Version
    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int version;

    @Column(name = "url", nullable = true)
    @UrlConstraint
    private String url;

    @DateConstraint
    @NotNull
    @Column(name = "modified_at", nullable = false)
    private String modifiedAt;

    @Column(name = "parent_id", nullable = true)
    private String parentId;

    @SizeConstraint
    @Enumerated(EnumType.STRING)
    @NotNull
    private ItemType type;

    @Column(name = "size", nullable = true)
    private Integer size;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SystemItemDto> children = new ArrayList<>();

    public void addChild(SystemItemDto child) {
        child.setParentId(this.getId());
        children.add(child);
    }

    public void removeChild(SystemItemDto child) {
        child.setParentId(null);
        children.remove(child);
    }

    public Integer getSize() {
        return getSizeRecursive(this);
    }

    private Integer getSizeRecursive(SystemItemDto dto) {
        if (dto.getType() == ItemType.FILE) return dto.size;
        int size = 0;
        for (var child : dto.getChildren()) {
            size += child.getSize();
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemItemDto that = (SystemItemDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
