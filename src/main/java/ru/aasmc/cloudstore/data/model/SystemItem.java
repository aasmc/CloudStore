package ru.aasmc.cloudstore.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import ru.aasmc.cloudstore.util.DateProcessor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "system_item")
public class SystemItem {
    @Id
    @Column(updatable = false, nullable = false)
    @NotNull
    private String id;

    @Version
    private int version;

    @Column(name = "url", nullable = true)
    private String url;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @Column(name = "modified_at", nullable = false)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    private ZonedDateTime modifiedAt;

    @ManyToOne(optional = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_item", nullable = true)
    private SystemItem parentItem;

    @NotNull
    @Column(name = "item_type")
    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Nullable
    @Column(name = "size", nullable = true)
    private int size;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SystemItem> children = new ArrayList<>();

    public SystemItem() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemItem that = (SystemItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
