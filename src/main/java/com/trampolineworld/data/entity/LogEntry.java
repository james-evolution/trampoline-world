package com.trampolineworld.data.entity;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.trampolineworld.data.service.LogEntryRepository;
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
	private Long targetOrderId;
	private String customerName;
	private String actionCategory;
	private String actionDetails;
	private Timestamp timestamp;
	
	public LogEntry() {
		
	}

	// Order targets.
	public LogEntry(LogEntryRepository logEntryRepository, UUID userId, String username, Long targetOrderId, String customerName, String actionCategory,
			String actionDetails, Timestamp timestamp) {
		this.userId = userId;
		this.username = username;
		this.targetOrderId = targetOrderId;
		this.setCustomerName(customerName);
		this.actionCategory = actionCategory;
		this.actionDetails = actionDetails;
		this.timestamp = timestamp;
		
		logEntryRepository.save(this);
		sendDiscordWebhookMessage(actionDetails + " for " + customerName);
	}
	// User targets.
	public LogEntry(LogEntryRepository logEntryRepository, UUID userId, String username, UUID targetUserId, String actionCategory,
			String actionDetails, Timestamp timestamp) {
		this.userId = userId;
		this.username = username;
		this.targetUserId = targetUserId;
		this.actionCategory = actionCategory;
		this.actionDetails = actionDetails;
		this.timestamp = timestamp;
		
		logEntryRepository.save(this);
		sendDiscordWebhookMessage(actionDetails);
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

	public Long getTargetOrderId() {
		return targetOrderId;
	}

	public void setTargetOrderId(Long targetOrderId) {
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

	public static void sendDiscordWebhookMessage(String message) {
		
		String webhookURL = "https://ptb.discord.com/api/webhooks/988942093059784734/AUdjyyXznFlN1T7IovybkPlu6h_HEdVK4gE80uTgvRiIKEg7UXrvQaHvLtbV66zuwFRY";
		
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