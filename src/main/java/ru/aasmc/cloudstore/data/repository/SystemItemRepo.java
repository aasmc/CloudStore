package ru.aasmc.cloudstore.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.aasmc.cloudstore.data.model.SystemItem;

public interface SystemItemRepo extends JpaRepository<SystemItem, String> {

}