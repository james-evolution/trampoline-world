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
  "where lower(m.topic) like lower(concat('%', :filterText, '%')) "
  + "or lower(m.text) like lower(concat('%', :filterText, '%'))"
  + "or lower(m.authorId) like lower(concat('%', :filterText, '%'))"
  )
  
  List<Message> search(@Param("filterText") String filterText);
}