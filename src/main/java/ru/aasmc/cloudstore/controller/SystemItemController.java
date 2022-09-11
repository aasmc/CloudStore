package ru.aasmc.cloudstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.aasmc.cloudstore.data.model.ItemType;
import ru.aasmc.cloudstore.data.model.dto.*;
import ru.aasmc.cloudstore.data.service.SystemItemService;
import ru.aasmc.cloudstore.exceptions.ItemNotFoundException;
import ru.aasmc.cloudstore.exceptions.ValidationException;
import ru.aasmc.cloudstore.util.DateProcessor;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class SystemItemController {

    private SystemItemService service;

    @Autowired
    public SystemItemController(SystemItemService service) {
        this.service = service;
    }

    @PostMapping(value = "imports", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void saveImports(@Valid @RequestBody ImportsDto imports,
                            BindingResult result) {

        if (result.hasErrors() || inputValidationFailed(imports)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
        }

        service.saveAll(imports);
    }

    private boolean inputValidationFailed(ImportsDto imports) {
        Map<ItemType, List<SystemItemDto>> byType = imports.getItems().stream()
                .collect(Collectors.groupingBy(SystemItemDto::getType));

        List<SystemItemDto> folders = byType.get(ItemType.FOLDER);
        boolean folderViolated = false;
        if (folders != null) {
            folderViolated = folders.parallelStream()
                    .anyMatch(item -> item.getSize() != null || item.getUrl() != null);
        }
        List<SystemItemDto> files = byType.get(ItemType.FILE);
        boolean fileViolated = false;
        if (files != null) {
            fileViolated = files.parallelStream()
                    .anyMatch(item -> item.getUrl().length() > 255 || item.getSize() < 1);
        }
        Set<String> ids = imports.getItems().stream()
                .map(SystemItemDto::getId)
                .collect(Collectors.toSet());
        boolean duplicateIdsFound = ids.size() != imports.getItems().size();

        return folderViolated || fileViolated || duplicateIdsFound;
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(path = "delete/{id}")
    public void delete(@PathVariable String id, @RequestParam String date) {
        if (!DateProcessor.checkDate(date)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
        }
        try {
            service.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ItemNotFoundException(HttpStatus.NOT_FOUND, "Item not found");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "nodes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SystemItemExtendedDto getItem(@PathVariable String id) {
        if (id == null || id.isEmpty() || id.isBlank()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
        }
        Optional<SystemItemExtendedDto> opt = service.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new ItemNotFoundException(HttpStatus.NOT_FOUND, "Item not found");
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "updates", produces = MediaType.APPLICATION_JSON_VALUE)
    public UpdateItemsWrapper getUpdates(@RequestParam String date) {
        if (!DateProcessor.checkDate(date)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
        }
        LocalDateTime before = DateProcessor.toDate(date);
        LocalDateTime after = before.minusDays(1);
        List<UpdateItemDto> updates = service.findUpdates(before, after);
        var result = new UpdateItemsWrapper();
        result.setItems(updates);
        return result;
    }

}
