package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.TrampolineOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TrampolineOrderService {

    private final TrampolineOrderRepository repository;

    @Autowired
    public TrampolineOrderService(TrampolineOrderRepository repository) {
        this.repository = repository;
    }

    public Optional<TrampolineOrder> get(UUID id) {
        return repository.findById(id);
    }

    public TrampolineOrder update(TrampolineOrder entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<TrampolineOrder> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
