package ru.aasmc.cloudstore.data.service;

import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;

import java.util.Optional;

public interface SystemItemService {

    Optional<SystemItemExtendedDto> findById(String id);

    void deleteById(String id);

    void saveAll(ImportsDto imports);
}
