package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.TrampolineOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrampolineOrderRepository extends JpaRepository<TrampolineOrder, UUID> {

}