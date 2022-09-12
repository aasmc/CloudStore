package ru.aasmc.cloudstore.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "history_item")
@Getter
@Setter
@NoArgsConstructor
public class HistoryItem {

    @EmbeddedId
    ModItemId id;

    @Column(name = "url", nullable = true)
    private String url;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name ="parent_id", nullable = true)
    private String parentId;

    @Column(name = "item_size", nullable = true)
    private Integer size;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryItem that = (HistoryItem) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url) && Objects.equals(date, that.date) && Objects.equals(parentId, that.parentId) && Objects.equals(size, that.size) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, date, parentId, size, type);
    }
}
