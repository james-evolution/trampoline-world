package com.trampolineworld.views.chat;

import com.vaadin.collaborationengine.CustomMessageInput;
import com.vaadin.collaborationengine.MessageManager;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.MessageRepository;
import com.trampolineworld.data.service.MessageService;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.data.service.WebhookRepository;
import com.trampolineworld.utilities.DiscordWebhook;
import com.trampolineworld.utilities.MessagePersister;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationMessage;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;

@PageTitle("Chat Room")
@Route(value = "chat", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "USER" })
public class ChatView extends VerticalLayout implements BeforeEnterObserver {

  private final UserService userService;
  private final UserRepository userRepository;
  private WebhookRepository webhookRepository;
  private MessageService messageService;
  private MessageRepository messageRepository;
  private String channelName;
  private CollaborationAvatarGroup avatarGroup;
  private CollaborationMessageList list;
  private UserInfo userInfo;
  private String currentTopic = "chat/#general";
  private static UserInfo loggedUserInfo;

  public ChatView(UserService userService, UserRepository userRepository, WebhookRepository webhookRepository,
      MessageService messageService, MessageRepository messageRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.webhookRepository = webhookRepository;
    this.messageRepository = messageRepository;
    this.messageService = messageService;
    addClassName("chat-view");
    setSpacing(false);

    // Gets currently logged in user.
    String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
    User currentUser = userRepository.findByUsername(currentUsername);

    // UserInfo is used by Collaboration Engine and is used to share details
    // of users to each other to able collaboration. Replace this with
    // information about the actual user that is logged, providing a user
    // identifier, and the user's real name. You can also provide the users
    // avatar by passing an url to the image as a third parameter, or by
    // configuring an `ImageProvider` to `avatarGroup`.
    userInfo = new UserInfo(currentUser.getId().toString(), currentUser.getDisplayName());
    userInfo.setImage(currentUser.getProfilePictureUrl());
    userInfo.setColorIndex(currentUser.getColorIndex());
    avatarGroup = new CollaborationAvatarGroup(userInfo, null);
    avatarGroup.getStyle().set("visibility", "visible");

    // Tabs allow us to change chat rooms.
    Tabs tabs = new Tabs(new Tab("#general"), new Tab("#notes"), new Tab("#issues"));
    tabs.setWidthFull();

    // `CollaborationMessageList` displays messages that are in a
    // Collaboration Engine topic. You should give in the user details of
    // the current user using the component, and a topic Id. Topic id can be
    // any freeform string. In this template, we have used the format
    // "chat/#general". Check
    // https://vaadin.com/docs/latest/ce/collaboration-message-list/#persisting-messages
    // for information on how to persisting are retrieving messages over
    // server restarts.
    list = new CollaborationMessageList(userInfo, "chat/#general");
    list.setWidthFull();
    list.addClassNames("chat-view-message-list");

    // MessageService messageService, MessageRepository messageRepository,
    // UserService userService
    MessageManager messageManager = createManagerWithPersister(
        new MessagePersister(messageService, messageRepository, userService));

    messageManager.setMessageHandler(context -> {
      CollaborationMessage message = context.getMessage();
      UserInfo user = message.getUser();
    });

    // `CollaborationMessageInput is a textfield and button, to be able to
    // submit new messages. To avoid having to set the same info into both
    // the message list and message input, the input takes in the list as an
    // constructor argument to get the information from there.

    // JAMES CUSTOM MESSAGE INPUT
    CustomMessageInput input = new CustomMessageInput(list);
    input.addClassNames("chat-view-message-input");
    input.setWidthFull();
    input.getContent().addSubmitListener(event -> {
      String message = event.getValue();
      messageManager.submit(message);
//      sendDiscordWebhookMessage(webhookRepository, userInfo.getName(), userInfo.getImage(), message);
    });

    // Add components to layout.
    add(avatarGroup, new Paragraph(), tabs, list, input);
    setSizeFull();
    expand(list);

    // Change the topic id of the chat when a new tab is selected
    tabs.addSelectedChangeListener(event -> {
      channelName = event.getSelectedTab().getLabel();
      list.setTopic("chat/" + channelName);
      currentTopic = "chat/" + channelName;
    });
  }

  public MessageManager createManagerWithPersister(MessagePersister persister) {
//    UserInfo localUser = new UserInfo("john");
//    String topicId = "chat/#general";
    return new MessageManager(this, userInfo, currentTopic, persister);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    // TODO Auto-generated method stub
    avatarGroup.setTopic("chat/" + channelName);
  }

  public static void sendDiscordWebhookMessage(WebhookRepository webhookRepository, String username, String avatarURL,
      String message) {

    String webhookURL = webhookRepository.findByWebhookName("Logs (Chat)").getWebhookUrl();

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
    webhook.setUsername(username);
    webhook.setAvatarUrl(avatarURL);
    webhook.setContent(message);
    webhook.setTts(false);

    try {
      webhook.execute();
    } catch (IOException e1) {
      System.out.println(e1.toString());
    }
  }
}