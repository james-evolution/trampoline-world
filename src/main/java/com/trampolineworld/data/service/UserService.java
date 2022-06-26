package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> get(UUID id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public List<User> findAll(String filterText) {
      if (filterText == null || filterText.isEmpty()) {
        return repository.findAll();
      }
      else {
        return repository.search(filterText);
      }
    }
    
    public List<User> findAllNoFilter() {
     return repository.findAll();
     }

    public Optional<User> findById(String id) {
      UUID uuid = UUID.fromString(id);
      return repository.findById(uuid);
    }       

}
