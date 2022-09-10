package ru.aasmc.cloudstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.aasmc.cloudstore.data.model.dto.BasicValidation;
import ru.aasmc.cloudstore.data.model.dto.ImportsDto;
import ru.aasmc.cloudstore.data.model.dto.SystemItemExtendedDto;
import ru.aasmc.cloudstore.data.service.SystemItemService;
import ru.aasmc.cloudstore.exceptions.ItemNotFoundException;
import ru.aasmc.cloudstore.exceptions.ValidationException;
import ru.aasmc.cloudstore.util.DateProcessor;

import javax.validation.Valid;
import java.util.Optional;

@Validated(BasicValidation.class)
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
        if (result.hasErrors()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
        }
        service.saveAll(imports);
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
        if (id  == null || id.isEmpty() || id.isBlank()) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, "Validation Failed");
        }
        Optional<SystemItemExtendedDto> opt = service.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new ItemNotFoundException(HttpStatus.NOT_FOUND, "Item not found");
    }

}
