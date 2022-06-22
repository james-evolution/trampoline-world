package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.LogEntry;
import com.trampolineworld.data.entity.TrampolineOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrampolineOrderRepository extends JpaRepository<TrampolineOrder, Long> {

	@Query(
	"select o from TrampolineOrder o " + 
	"where lower(o.firstName) like lower(concat('%', :filterText, '%')) "
	+ "or lower(o.lastName) like lower(concat('%', :filterText, '%'))"
	+ "or lower(o.email) like lower(concat('%', :filterText, '%'))"
	+ "or lower(o.phoneNumber) like lower(concat('%', :filterText, '%'))"
	)
	List<TrampolineOrder> search(@Param("filterText") String filterText);
	
	Optional<TrampolineOrder> findById(Long orderId);
}