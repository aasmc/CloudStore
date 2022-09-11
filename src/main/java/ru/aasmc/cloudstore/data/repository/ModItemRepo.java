package ru.aasmc.cloudstore.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.aasmc.cloudstore.data.model.ModItem;
import ru.aasmc.cloudstore.data.model.ModItemId;

public interface ModItemRepo extends JpaRepository<ModItem, ModItemId> {
    @Modifying
    @Query("delete from mod_item m where m.id.modifiedItemId=:modifiedItemId")
    void deleteAllByModifiedItemId(String modifiedItemId);
}
