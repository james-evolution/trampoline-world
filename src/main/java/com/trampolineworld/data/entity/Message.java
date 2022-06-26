package com.trampolineworld.data.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trampolineworld.data.service.MessageRepository;

@Entity
@Table(name = "chat_logs")
public class Message extends AbstractEntityUUID {

  private String topic;
  private String text;
  private String authorId;
  @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Instant timestamp;

  public Message() {
  }

  public Message(MessageRepository messageRepository, String topic, String text, User author) {
    this.topic = topic;
    this.text = text;
    this.authorId = author.getId().toString();
    messageRepository.save(this);
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

}
