package ru.aasmc.cloudstore.data.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "system_item")
@Getter
@Setter
public class SystemItem {
    @Id
    @Column(name = "id", nullable = false)
    String id;

    @Column(name = "url", nullable = true)
    String url;

    @Version
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    int version;

    @Column(name = "modified_at", nullable = false)
    String modifiedAt;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_id", nullable = true)
    SystemItem parentItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    ItemType type;

    @Column(name = "item_size", nullable = true)
    Integer size;

    @OneToMany(mappedBy = "parentItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<SystemItem> children = new ArrayList<>();

    public void addChild(SystemItem child) {
        child.setParentItem(this);
        children.add(child);
    }

    public void removeChild(SystemItem child) {
        child.setParentItem(null);
        children.remove(child);
    }

    public Integer getSize() {
        return getSizeRecursive(this);
    }

    private int getSizeRecursive(SystemItem item) {
        if (item.getType() == ItemType.FILE) return size;
        int size = 0;
        for (var elem : item.getChildren()) {
            size += elem.getSize();
        }
        return size;
    }
}
