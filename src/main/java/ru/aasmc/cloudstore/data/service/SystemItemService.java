package ru.aasmc.cloudstore.data.service;

import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;
import ru.aasmc.cloudstore.data.model.dto.UpdateItemDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SystemItemService {

    Optional<SystemItemExtendedDto> findById(String id);

    void deleteById(String id);

    void saveAll(ImportsDto imports);

    List<UpdateItemDto> findUpdates(LocalDateTime before, LocalDateTime after);

    List<UpdateItemDto> findHistoryByItemId(String itemId);

    List<UpdateItemDto> findHistoryByItemIdAndDate(String itemId,
                                                   LocalDateTime from,
                                                   LocalDateTime to);
}
