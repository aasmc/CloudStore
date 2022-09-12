package ru.aasmc.cloudstore.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.aasmc.cloudstore.data.model.HistoryItem;

import java.time.LocalDateTime;
import java.util.Set;

public interface HistoryItemRepo extends JpaRepository<HistoryItem, Long> {
    @Modifying
    @Query("delete from history_item hi where hi.itemId =:itemId")
    void deleteAllByItemId(String itemId);


    @Query("select distinct hi from history_item hi " +
            "inner join system_item i on hi.itemId = i.id " +
            "where i.id = :itemId")
    Set<HistoryItem> findAllHistoryByItemId(String itemId);

    @Query("select distinct hi from history_item hi " +
            "inner join system_item i on hi.itemId = i.id " +
            "where i.id = :itemId " +
            "and hi.modifiedAt >= :from " +
            "and hi.modifiedAt < :to")
    Set<HistoryItem> findAllHistoryByItemIdAndDate(String itemId, LocalDateTime from, LocalDateTime to);

}
