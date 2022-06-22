package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.LogEntry;
import com.trampolineworld.data.entity.User;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LogEntryRepository extends JpaRepository<LogEntry, UUID> {

	@Query(
	"select logEntry from LogEntry logEntry " + 
	"where logEntry.userId like concat('%', :filterText, '%')"
	+ "or lower(logEntry.username) like lower(concat('%', :filterText, '%'))"
	+ "or logEntry.targetUserId like concat('%', :filterText, '%')"
	+ "or logEntry.targetOrderId like concat('%', :filterText, '%')"
	+ "or lower(logEntry.actionCategory) like lower(concat('%', :filterText, '%'))"
	)
	
//	@Query(
//	"select u from LogEntry u " + 
//	"where lower(u.username) like lower(concat('%', :filterText, '%')) "
//	)
	
//	@Query(
//			  value = "SELECT * FROM LogEn u WHERE u.status = 1", 
//			  nativeQuery = true)	
	
	List<LogEntry> search(@Param("filterText") String filterText);
	
	LogEntry findByUserId(UUID userId);
    LogEntry findByUsername(String username);
    LogEntry findByTargetUserId(UUID targetUserId);
    LogEntry findByTargetOrderId(Long targetOrderId);
    LogEntry findByActionCategory(String actionCategory);
}