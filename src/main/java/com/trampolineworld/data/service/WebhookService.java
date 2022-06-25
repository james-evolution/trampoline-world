package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.Webhook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private final WebhookRepository repository;

    @Autowired
    public WebhookService(WebhookRepository repository) {
        this.repository = repository;
    }

    public Optional<Webhook> get(UUID id) {
        return repository.findById(id);
    }

    public Webhook update(Webhook entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Webhook> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public List<Webhook> findAll(String filterText) {
      if (filterText == null || filterText.isEmpty()) {
        return repository.findAll();
      }
      else {
        return repository.search(filterText);
      }
    }
    
    public List<Webhook> findAllNoFilter() {
     return repository.findAll();
     }       

}
