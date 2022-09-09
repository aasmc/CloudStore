package ru.aasmc.cloudstore.data.service;

import ru.aasmc.cloudstore.data.model.SystemItemDto;

import java.util.List;
import java.util.Optional;

public interface SystemItemService {

    Optional<SystemItemDto> findById(String id);

    void deleteById(String id);

    void saveAll(List<SystemItemDto> items);
}
