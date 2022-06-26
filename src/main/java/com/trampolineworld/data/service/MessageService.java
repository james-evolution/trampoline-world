package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository repository;

    @Autowired
    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public Optional<Message> get(UUID id) {
        return repository.findById(id);
    }

    public Message update(Message entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Message> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    
    public List<Message> findAll(String filterText) {
      if (filterText == null || filterText.isEmpty()) {
        return repository.findAll();
      }
      else {
        return repository.search(filterText);
      }
    }
    
    public List<Message> findAllNoFilter() {
     return repository.findAll();
     }

//    public Stream<CollaborationMessage> findAllByTopicSince(String topicId, Instant since) {
//      
//      List<Message> allMessages = repository.findAll();
//      List<CollaborationMessage> filteredMessages = new ArrayList<CollaborationMessage>();
//      
//      for (Message m : allMessages) {
//        if (m.getTimetamp().compareTo(since) > 0) {
//          UserInfo userInfo = new UserInfo(m.getAuthor().getId().toString(), m.getAuthor().getDisplayName(), m.getAuthor().getProfilePictureUrl());
//          CollaborationMessage cm = new CollaborationMessage(userInfo, m.getText(), m.getTimetamp());
//          filteredMessages.add(cm);
//        }
//      }
//      
//      Stream<CollaborationMessage> messageStream = filteredMessages.stream();
//      return messageStream;
//    }       
    
    public Stream<Message> findAllByTopicSince(String topicId, Instant since) {
      
//      List<Message> allMessages = repository.findAll();
      List<Message> allMessages = repository.search(topicId);
      List<Message> filteredMessages = new ArrayList<Message>();
      
      for (Message m : allMessages) {
        if (m.getTimestamp().compareTo(since) >= 0) {
          filteredMessages.add(m);
        }
      }
      
      Stream<Message> messageStream = filteredMessages.stream();
      return messageStream;
    }

}
