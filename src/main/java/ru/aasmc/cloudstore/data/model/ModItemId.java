package ru.aasmc.cloudstore.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.aasmc.cloudstore.util.DateProcessor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ModItemId implements Serializable {

    @Column(name = "modified_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    private LocalDateTime modifiedAt;

    @Column(name = "modified_item_id")
    private String modifiedItemId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModItemId modItemId = (ModItemId) o;
        return Objects.equals(modifiedAt, modItemId.modifiedAt) && Objects.equals(modifiedItemId, modItemId.modifiedItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifiedAt, modifiedItemId);
    }
}
