package ru.aasmc.cloudstore.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.aasmc.cloudstore.data.model.SystemItem;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SystemItemRepo extends JpaRepository<SystemItem, String> {
    @Query("select distinct i from system_item i " +
            "inner join mod_item m on m.id.modifiedItemId = i.id " +
            "where m.id.modifiedAt <= :before and m.id.modifiedAt >= :after")
    List<SystemItem> findByModificationDate(LocalDateTime before, LocalDateTime after);
}