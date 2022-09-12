package ru.aasmc.cloudstore.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aasmc.cloudstore.data.model.*;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;
import ru.aasmc.cloudstore.data.model.dto.UpdateItemDto;
import ru.aasmc.cloudstore.data.repository.HistoryItemRepo;
import ru.aasmc.cloudstore.data.repository.ModItemRepo;
import ru.aasmc.cloudstore.data.repository.SystemItemRepo;
import ru.aasmc.cloudstore.util.DateProcessor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemItemServiceImpl implements SystemItemService {

    private SystemItemRepo repo;
    private ModItemRepo modItemRepo;
    private HistoryItemRepo historyItemRepo;

    @Autowired
    public SystemItemServiceImpl(SystemItemRepo repo,
                                 ModItemRepo modItemRepo,
                                 HistoryItemRepo historyItemRepo) {
        this.repo = repo;
        this.modItemRepo = modItemRepo;
        this.historyItemRepo = historyItemRepo;
    }

    @Override
    public Optional<SystemItemExtendedDto> findById(String id) {
        Optional<SystemItem> optional = repo.findById(id);
        if (optional.isEmpty()) return Optional.empty();
        SystemItem entity = optional.get();
        return Optional.of(mapFromEntity(entity));
    }

    private SystemItemExtendedDto mapFromEntity(SystemItem entity) {
        var extended = new SystemItemExtendedDto();
        extended.setId(entity.getId());
        extended.setUrl(entity.getUrl());
        extended.setSize(entity.getSize());
        extended.setType(entity.getType());
        extended.setDate(entity.getModifiedAt());

        if (entity.getParentItem() != null) {
            extended.setParentId(entity.getParentItem().getId());
        } else {
            extended.setParentId(null);
        }

        if (entity.getType() == ItemType.FILE) {
            extended.setChildren(null);
        } else {
            List<SystemItemExtendedDto> children = entity.getChildren().stream()
                    .map(this::mapFromEntity)
                    .collect(Collectors.toList());
            extended.setChildren(children);
        }
        return extended;
    }

    @Override
    public void deleteById(String id) {
        repo.deleteById(id);
        modItemRepo.deleteAllByModifiedItemId(id);
        historyItemRepo.deleteAllByItemId(id);
    }

    @Override
    public void saveAll(ImportsDto imports) {
        String updateDate = imports.getUpdateDate();
        Map<String, SystemItem> cache = new HashMap<>();
        for (var elem : imports.getItems()) {
            SystemItem entity;
            if (cache.containsKey(elem.getId())) {
                entity = cache.get(elem.getId());
            } else {
                entity = buildFromDto(elem, updateDate, imports.getItems(), cache);
            }
            // Save persisted entity!
            SystemItem saved = repo.save(entity);
            saveModItem(saved, updateDate);
        }
        // save separately to preserve info about size
        for (var entity : cache.values()) {
            saveHistoryItem(entity, updateDate);
        }
    }


    private SystemItem buildFromDto(SystemItemDto elem,
                                    String updateDate,
                                    List<SystemItemDto> items,
                                    Map<String, SystemItem> cache) {
        var entity = new SystemItem();
        entity.setModifiedAt(updateDate);
        entity.setId(elem.getId());
        entity.setUrl(elem.getUrl());
        entity.setType(elem.getType());
        entity.setSize(elem.getSize());

        if (elem.getParentId() != null) {
            if (cache.containsKey(elem.getParentId())) {
                addChildTo(cache.get(elem.getParentId()), entity);
            } else {
                List<SystemItemDto> parents = items.stream()
                        .filter(i -> i.getId().equals(elem.getParentId()))
                        .collect(Collectors.toList());
                if (!parents.isEmpty()) {
                    assert parents.size() == 1;
                    var parent = parents.get(0);
                    var parentEntity = buildFromDto(parent, updateDate, items, cache);
                    addChildTo(parentEntity, entity);
                } else {
                    Optional<SystemItem> dbParent = repo.findById(elem.getParentId());
                    dbParent.ifPresent(item -> {
                        addChildTo(item, entity);
                        item.setModifiedAt(updateDate);
                        saveModItem(item, updateDate);
                        saveHistoryItem(item, updateDate);
                        propagateModificationUp(item, updateDate);
                    });
                }
            }
        }
        cache.putIfAbsent(elem.getId(), entity);
        return entity;
    }

    @Override
    public List<UpdateItemDto> findUpdates(LocalDateTime before, LocalDateTime after) {
        var systemItems = repo.findByModificationDate(before, after);
        return buildUpdateItemDtos(systemItems);
    }

    private List<UpdateItemDto> buildUpdateItemDtos(List<SystemItem> systemItems) {
        if (!systemItems.isEmpty()) {
            var result = new ArrayList<UpdateItemDto>();
            for (var item : systemItems) {
                var dto = new UpdateItemDto();
                dto.setId(item.getId());
                dto.setType(item.getType());
                dto.setUrl(item.getUrl());
                dto.setSize(item.getSize());
                SystemItem parentItem = item.getParentItem();
                if (parentItem != null) {
                    dto.setParentId(parentItem.getId());
                } else {
                    dto.setParentId(null);
                }
                dto.setDate(item.getModifiedAt());
                result.add(dto);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<UpdateItemDto> findHistoryByItemId(String itemId) {
        Set<HistoryItem> history = historyItemRepo.findAllHistoryByItemId(itemId);
        return buildUpdateItemDtosFromHystory(history);
    }

    private List<UpdateItemDto> buildUpdateItemDtosFromHystory(Set<HistoryItem> history) {
        if (!history.isEmpty()) {
            var result = new ArrayList<UpdateItemDto>();
            for (var item : history) {
                var dto = new UpdateItemDto();
                dto.setId(item.getItemId());
                dto.setType(item.getType());
                dto.setUrl(item.getUrl());
                dto.setSize(item.getSize());
                dto.setParentId(item.getParentId());
                dto.setDate(item.getDate());
                result.add(dto);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<UpdateItemDto> findHistoryByItemIdAndDate(String itemId, LocalDateTime from, LocalDateTime to) {
        Set<HistoryItem> history = historyItemRepo.findAllHistoryByItemIdAndDate(itemId, from, to);
        return buildUpdateItemDtosFromHystory(history);
    }


    private void saveModItem(SystemItem systemItem, String updateDate) {
        ModItem modItem = new ModItem();
        ModItemId id = new ModItemId();
        id.setModifiedAt(DateProcessor.toDate(updateDate));
        id.setModifiedItemId(systemItem.getId());
        modItem.setId(id);
        modItemRepo.save(modItem);
    }

    private void saveHistoryItem(SystemItem systemItem, String updateDate) {
        var historyItem = new HistoryItem();
        historyItem.setItemId(systemItem.getId());
        historyItem.setDate(updateDate);
        historyItem.setType(systemItem.getType());
        historyItem.setUrl(systemItem.getUrl());
        var parent = systemItem.getParentItem();
        if (parent != null) {
            historyItem.setParentId(parent.getId());
        } else {
            historyItem.setParentId(null);
        }
        historyItem.setModifiedAt(DateProcessor.toDate(updateDate));
        Integer size = systemItem.getSize();
        if (size == 0) { // this is a folder with no children
            historyItem.setSize(null);
        } else {
            historyItem.setSize(size);
        }
        historyItemRepo.save(historyItem);
    }

    private void propagateModificationUp(SystemItem parentItem, String modifiedAt) {
        while (parentItem != null) {
            parentItem.setModifiedAt(modifiedAt);
            saveModItem(parentItem, modifiedAt);
            saveHistoryItem(parentItem, modifiedAt);
            parentItem = parentItem.getParentItem();
        }
    }

    private void addChildTo(SystemItem parent, SystemItem child) {
        parent.addChild(child);
    }
}
