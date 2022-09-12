package ru.aasmc.cloudstore.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.aasmc.cloudstore.util.DateProcessor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "history_item")
@Getter
@Setter
@NoArgsConstructor
public class HistoryItem {
    @Id
    @GeneratedValue(generator = "seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name="seq_generator", sequenceName = "SEQ_HISTORY_ITEM", allocationSize=1)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private String itemId;

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

    @Column(name = "modified_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    private LocalDateTime modifiedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryItem that = (HistoryItem) o;
        return Objects.equals(itemId, that.itemId) && Objects.equals(url, that.url) && Objects.equals(date, that.date) && Objects.equals(parentId, that.parentId) && Objects.equals(size, that.size) && type == that.type && Objects.equals(modifiedAt, that.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, url, date, parentId, size, type, modifiedAt);
    }
}
