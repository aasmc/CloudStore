package ru.aasmc.cloudstore.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aasmc.cloudstore.data.model.SystemItemDto;
import ru.aasmc.cloudstore.data.repository.SystemItemRepo;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SystemItemServiceImpl implements SystemItemService {

    private SystemItemRepo repo;

    @Autowired
    public SystemItemServiceImpl(SystemItemRepo repo) {
        this.repo = repo;
    }


    @Override
    public Optional<SystemItemDto> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public void deleteById(String id) {
        repo.deleteById(id);
    }

    @Override
    public void saveAll(List<SystemItemDto> items) {
        repo.saveAll(items);
    }
}
