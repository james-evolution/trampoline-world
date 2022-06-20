package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.User;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	@Query(
	"select u from User u " + 
	"where lower(u.username) like lower(concat('%', :filterText, '%')) "
	+ "or lower(u.name) like lower(concat('%', :filterText, '%'))"
	+ "or lower(u.email) like lower(concat('%', :filterText, '%'))"
	)
	List<User> search(@Param("filterText") String filterText);
	
    User findByUsername(String username);
}