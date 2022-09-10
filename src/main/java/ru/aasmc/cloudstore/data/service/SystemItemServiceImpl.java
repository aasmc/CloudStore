package ru.aasmc.cloudstore.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.SystemItem;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;
import ru.aasmc.cloudstore.data.repository.SystemItemRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemItemServiceImpl implements SystemItemService {

    private SystemItemRepo repo;

    @Autowired
    public SystemItemServiceImpl(SystemItemRepo repo) {
        this.repo = repo;
    }


    @Override
    @Transactional(readOnly = true)
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
    }

    @Override
    public void saveAll(ImportsDto imports) {
        String updateDate = imports.getUpdateDate();
        Map<String, SystemItem> cache = new HashMap<>();
        for (var elem : imports.getItems()) {
            var entity = buildFromDto(elem, updateDate, imports.getItems(), cache);
            repo.save(entity);
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
                }
            }
        }
        cache.put(elem.getId(), entity);
        return entity;
    }

    private void addChildTo(SystemItem parent, SystemItem child) {
        parent.addChild(child);
    }

}
