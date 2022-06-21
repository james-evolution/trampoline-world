package com.vaadin.collaborationengine;

import com.trampolineworld.utilities.DiscordWebhook;

/*
 * (Modified by James 6/21/2022)
 * 
 * Copyright 2020-2022 Vaadin Ltd.
 *
 * This program is available under Commercial Vaadin Runtime License 1.0
 * (CVRLv1).
 *
 * For the full License, see http://vaadin.com/license/cvrl-1
 */
import java.io.IOException;
import java.util.Objects;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.shared.Registration;

/**
 * Extension of the {@link MessageInput} component which integrates with the
 * {@link CollaborationMessageList}. The user can type a message and submit it.
 * The messages will be displayed in any {@link CollaborationMessageList} that
 * is connected to the same topic as the list passed as the argument of this
 * component constructor. The text area and button will be disabled while the
 * connection to the topic is not active or the topic is set to
 * <code>null</code> (see {@link CollaborationMessageList#setTopic(String)}).
 *
 * @author Vaadin Ltd
 * @since 3.1
 */
public class CustomMessageInput extends Composite<MessageInput>
        implements HasSize, HasStyle {

    static {
        UsageStatistics.markAsUsed(
                CollaborationEngine.COLLABORATION_ENGINE_NAME
                        + "/CollaborationMessageInput",
                CollaborationEngine.COLLABORATION_ENGINE_VERSION);
    }

    /**
     * Creates a new collaboration message input component which submits
     * messages to the provided {@link CollaborationMessageList}.
     *
     * @param list
     *            the list which will display the submitted messages, not null
     * @param userInfo 
     */
    public CustomMessageInput(CollaborationMessageList list, UserInfo userInfo) {
        Objects.requireNonNull(list,
                "A list instance to connect this component to is required");
        getContent().setEnabled(false);
        list.setSubmitter(activationContext -> {
            getContent().setEnabled(true);
            Registration registration = getContent().addSubmitListener(event -> {
            	activationContext.appendMessage(event.getValue());
            	sendDiscordWebhookMessage(userInfo.getName(), userInfo.getImage(), event.getValue());
            });
            return () -> {
                registration.remove();
                getContent().setEnabled(false);
            };
        });
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content returned by this method will not update
     * the component if not set back using
     * {@link MessageInput#setI18n(MessageInputI18n)}.
     *
     * @return the i18n object, or {@code null} if one has not been set with
     *         {@link #setI18n(MessageInputI18n)}
     */
    public MessageInputI18n getI18n() {
        return getContent().getI18n();
    }

    /**
     * Sets the internationalization properties for this component. It enabled
     * you to customize and translate the language used in the message input.
     * <p>
     * Note: updating the object properties after setting the i18n will not
     * update the component. To make the changes effective, you need to set the
     * updated object again.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(MessageInputI18n i18n) {
        getContent().setI18n(i18n);
    }
    
    public static void sendDiscordWebhookMessage(String username, String avatarURL, String message) {
    	
    	String webhookURL = "https://ptb.discord.com/api/webhooks/988770668554375240/r-W13i9JLGQxmvV-6UzLiPVtTCzKBzG_jNUaJiwUtislZ4t_7MqflRb3uPTW1A93SjDL";
    
    	DiscordWebhook webhook = new DiscordWebhook(webhookURL);
    	webhook.setUsername(username);
    	webhook.setAvatarUrl(avatarURL);
    	webhook.setContent(message);
    	webhook.setTts(true);

		try {
			webhook.execute();
			Notification.show("Message sent successfully!", 4000, Position.TOP_CENTER)
			.addThemeVariants(NotificationVariant.LUMO_SUCCESS);				
		} catch (IOException e1) {
			e1.printStackTrace();
			Notification.show("Message failed to send!", 4000, Position.TOP_CENTER)
			.addThemeVariants(NotificationVariant.LUMO_ERROR);				
		}
    }     
}

