package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.LogEntry;
import com.trampolineworld.data.entity.TrampolineOrder;

import java.util.Collections;
import java.util.Comparator;
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
    
  private class SortByTimestamp implements Comparator<LogEntry> {
    // Sort by the most recent timestamp.
    public int compare(LogEntry a, LogEntry b) {
      return (int) (b.getTimestamp().compareTo(a.getTimestamp()));
    }
  }
  
    public List<LogEntry> findAll(String filterText) {
      if (filterText == null || filterText.isEmpty()) {
        List<LogEntry> allLogs = repository.findAll();
        Collections.sort(allLogs, new SortByTimestamp());
        return allLogs;
//        return repository.findAll();
      }
      else {
        List<LogEntry> allFilteredLogs = repository.search(filterText);
        Collections.sort(allFilteredLogs, new SortByTimestamp());
        return allFilteredLogs;
//        return repository.search(filterText);
      }
    }
    
    public List<LogEntry> findAllNoFilter() {
    List<LogEntry> allLogs = repository.findAll();
    Collections.sort(allLogs, new SortByTimestamp());
    return allLogs;
//     return repository.findAll();
     }       

}
