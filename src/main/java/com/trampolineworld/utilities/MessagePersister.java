package com.trampolineworld.utilities;

import java.util.stream.Stream;

import com.trampolineworld.data.entity.Message;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.MessageRepository;
import com.trampolineworld.data.service.MessageService;
import com.trampolineworld.data.service.UserService;
import com.vaadin.collaborationengine.CollaborationMessage;
import com.vaadin.collaborationengine.CollaborationMessagePersister;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class MessagePersister implements CollaborationMessagePersister {

  private final MessageService messageService;
  private final MessageRepository messageRepository;
  private final UserService userService;

  public MessagePersister(MessageService messageService, MessageRepository messageRepository, UserService userService) {
    this.messageService = messageService;
    this.userService = userService;
    this.messageRepository = messageRepository;
  }
  
  @Override
  public Stream<CollaborationMessage> fetchMessages(FetchQuery query) {
      return messageService
              .findAllByTopicSince(query.getTopicId(), query.getSince())
              .map(messageEntity -> {
//                  User author = messageEntity.getAuthor(); 
                  UserInfo userInfo = new UserInfo(messageEntity.getAuthorId(),
                          messageEntity.getAuthorName(), messageEntity.getAuthorAvatarUrl());
                  userInfo.setColorIndex(messageEntity.getAuthorColorIndex());

                  return new CollaborationMessage(userInfo,
                          messageEntity.getText(), messageEntity.getTimestamp());
              });
  }

  @Override
  public void persistMessage(PersistRequest request) {
    CollaborationMessage message = request.getMessage();

    Message messageEntity = new Message();
    messageEntity.setTopic(request.getTopicId());
    messageEntity.setText(message.getText());
    messageEntity.setAuthorId(message.getUser().getId());
    messageEntity.setAuthorName(message.getUser().getName());
    messageEntity.setAuthorAvatarUrl(message.getUser().getImage());
    messageEntity.setAuthorColorIndex(message.getUser().getColorIndex());

    // Set the time from the message only as a fallback option if your
    // database can't automatically add an insertion timestamp:
    // messageEntity.setTime(message.getTime());
    messageRepository.save(messageEntity);
  }
}