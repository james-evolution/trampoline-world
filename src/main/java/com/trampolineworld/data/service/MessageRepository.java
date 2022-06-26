package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.Message;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  @Query(
  "select m from Message m " + 
  "where lower(m.topic) like lower(concat('%', :topic, '%')) "
  )
  
  List<Message> search(@Param("topic") String filterText);
}