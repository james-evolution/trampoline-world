package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.LogEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LogEntryService {

    private final LogEntryRepository repository;

    @Autowired
    public LogEntryService(LogEntryRepository repository) {
        this.repository = repository;
    }

    public LogEntry get(UUID id) {
        return repository.findByUserId(id);
    }

    public LogEntry update(LogEntry entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<LogEntry> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public List<LogEntry> findAll(String filterText) {
    	if (filterText == null || filterText.isEmpty()) {
    		return repository.findAll();
    	}
    	else {
    		return repository.search(filterText);
    	}
    }
    
    public List<LogEntry> findAllNoFilter() {
 		return repository.findAll();
     }       

}
