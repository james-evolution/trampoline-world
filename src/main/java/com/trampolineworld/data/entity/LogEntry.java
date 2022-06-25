package com.trampolineworld.data.entity;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.trampolineworld.data.service.LogEntryRepository;
import com.trampolineworld.data.service.WebhookRepository;
import com.trampolineworld.utilities.DiscordWebhook;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;

@Entity
@Table(name = "audit_logs")
public class LogEntry extends AbstractEntityUUID {

    @Type(type = "uuid-char")
  private UUID userId;
  private String username;
    @Type(type = "uuid-char")
  private UUID targetUserId;
    // Making this a string so we can store multiple order ids via collections toString()
  private String targetOrderId;
  private String customerName;
  private String actionCategory;
  private String actionDetails;
  private Timestamp timestamp;
  
  public LogEntry() {
    
  }

  // Order targets.
  public LogEntry(LogEntryRepository logEntryRepository, WebhookRepository webhookRepository, UUID userId, String username, String targetOrderId, String customerName, String actionCategory,
      String actionDetails) {
    this.userId = userId;
    this.username = username;
    this.targetOrderId = targetOrderId;
    this.setCustomerName(customerName);
    this.actionCategory = actionCategory;
    this.actionDetails = actionDetails;
    this.timestamp = new Timestamp(new Date().getTime());
    
    logEntryRepository.save(this);
    sendDiscordWebhookMessage(webhookRepository, actionDetails + " at " + timestamp.toString());
  }
  // User targets.
  public LogEntry(LogEntryRepository logEntryRepository, WebhookRepository webhookRepository, UUID userId, String username, UUID targetUserId, String actionCategory,
      String actionDetails) {
    this.userId = userId;
    this.username = username;
    this.targetUserId = targetUserId;
    this.actionCategory = actionCategory;
    this.actionDetails = actionDetails;
    this.timestamp = new Timestamp(new Date().getTime());
    
    logEntryRepository.save(this);
    sendDiscordWebhookMessage(webhookRepository, actionDetails + " at " + timestamp.toString());
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UUID getTargetUserId() {
    return targetUserId;
  }

  public void setTargetUserId(UUID targetUserId) {
    this.targetUserId = targetUserId;
  }

  public String getTargetOrderId() {
    return targetOrderId;
  }

  public void setTargetOrderId(String targetOrderId) {
    this.targetOrderId = targetOrderId;
  }

  public String getActionCategory() {
    return actionCategory;
  }

  public void setActionCategory(String actionCategory) {
    this.actionCategory = actionCategory;
  }

  public String getActionDetails() {
    return actionDetails;
  }

  public void setActionDetails(String actionDetails) {
    this.actionDetails = actionDetails;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
  

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public static void sendDiscordWebhookMessage(WebhookRepository webhookRepository, String message) {
    
    String webhookURL = webhookRepository.findByWebhookName("Logs (Audit)").getWebhookUrl();
    
    // Trim leading & trailing whitespaces.
    webhookURL = webhookURL.trim();
    // Check for null or empty URL, if so - return, don't attempt to send.
    if (webhookURL.isEmpty() || webhookURL == null || webhookURL.equals("") || webhookURL == "") {
      System.out.println("URL is empty.");
      return;
    }
    // Log output.
    System.out.println("Attempting to send webhook message.");
    System.out.println(webhookURL);
    
    // Create & send webhook.
    DiscordWebhook webhook = new DiscordWebhook(webhookURL);
    webhook.setContent(message);
    webhook.setTts(false);
    
    try {
      webhook.execute();
    } catch (IOException e1) {
      System.out.println(e1.toString());
    }
  
  }
}
