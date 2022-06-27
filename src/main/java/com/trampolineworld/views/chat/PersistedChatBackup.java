//package com.trampolineworld.views.chat;
//
//import com.vaadin.collaborationengine.CustomMessageInput;
//import com.vaadin.collaborationengine.MessageManager;
//import com.trampolineworld.data.entity.User;
//import com.trampolineworld.data.service.MessageRepository;
//import com.trampolineworld.data.service.MessageService;
//import com.trampolineworld.data.service.UserRepository;
//import com.trampolineworld.data.service.UserService;
//import com.trampolineworld.data.service.WebhookRepository;
//import com.trampolineworld.utilities.DiscordWebhook;
//import com.trampolineworld.utilities.MessagePersister;
//import com.trampolineworld.views.MainLayout;
//import com.vaadin.collaborationengine.CollaborationAvatarGroup;
//import com.vaadin.collaborationengine.CollaborationMessageList;
//import com.vaadin.collaborationengine.UserInfo;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.tabs.Tab;
//import com.vaadin.flow.component.tabs.Tabs;
//import com.vaadin.flow.router.BeforeEnterEvent;
//import com.vaadin.flow.router.BeforeEnterObserver;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.server.VaadinRequest;
//
//import java.io.IOException;
//
//import javax.annotation.security.RolesAllowed;
//
//@PageTitle("Chat Room")
//@Route(value = "chat", layout = MainLayout.class)
//@RolesAllowed({ "ADMIN", "USER" })
//public class ChatView extends VerticalLayout implements BeforeEnterObserver {
//
//  private final UserService userService;
//  private final UserRepository userRepository;
//  private WebhookRepository webhookRepository;
//  private MessageService messageService;
//  private MessageRepository messageRepository;
//  private String channelName;
//  private CollaborationAvatarGroup avatarGroup;
//  private CollaborationMessageList list;
//  private UserInfo userInfo;
//  private String currentTopic = "chat/#general";
//  
//  private MessageManager messageManagerGeneral;
//  private MessageManager messageManagerNotes;
//  private MessageManager messageManagerIssues;
//
//  public ChatView(UserService userService, UserRepository userRepository, WebhookRepository webhookRepository,
//      MessageService messageService, MessageRepository messageRepository) {
//    this.userService = userService;
//    this.userRepository = userRepository;
//    this.webhookRepository = webhookRepository;
//    this.messageRepository = messageRepository;
//    this.messageService = messageService;
//    addClassName("chat-view");
//    setSpacing(false);
//
//    // Gets the currently logged in user.
//    String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
//    User currentUser = userRepository.findByUsername(currentUsername);
//
//    // Create the user info object and avatar group to display which users are currently viewing the chat.
//    userInfo = new UserInfo(currentUser.getId().toString(), currentUser.getDisplayName(), currentUser.getProfilePictureUrl());
//    userInfo.setColorIndex(currentUser.getColorIndex());
//    avatarGroup = new CollaborationAvatarGroup(userInfo, null);
//    avatarGroup.getStyle().set("visibility", "visible");
//    avatarGroup.getStyle().set("margin-bottom", "10px");
//
//    // Tabs allow us to change chat rooms.
//    Tabs tabs = new Tabs(new Tab("#general"), new Tab("#notes"), new Tab("#issues"));
//    tabs.setWidthFull();
//
//    // Create the message list & set the initial topic.
//    list = new CollaborationMessageList(userInfo, "chat/#general");
//    list.addClassNames("chat-view-message-list");
//    list.setWidthFull();
//
//    createMessageManagersWithPersisters();
//    
//    // JAMES' CUSTOM MESSAGE INPUT. This component is both a text field & a button.
//    CustomMessageInput input = new CustomMessageInput(list);
//    input.addClassNames("chat-view-message-input");
//    input.setWidthFull();
//    input.getContent().addSubmitListener(event -> {
//      String message = event.getValue();
//      
//      if (currentTopic.equals("chat/#general")) {
//        messageManagerGeneral.submit(message);
//        System.out.println("Trying to store a general message");
//      }
//      else if (currentTopic.equals("chat/#notes")) {
//        System.out.println("Trying to store a note");
//        messageManagerNotes.submit(message);
//      }
//      else if (currentTopic.equals("chat/#issues")) {
//        messageManagerIssues.submit(message);
//        System.out.println("Trying to store an issue");
//      }
////      sendDiscordWebhookMessage(webhookRepository, userInfo.getName(), userInfo.getImage(), message);
//    });
//
//    // Add components to layout.
//    add(avatarGroup, tabs, list, input);
//    setSizeFull();
//    expand(list);
//
//    // Change the topic id of the chat when a new tab is selected
//    tabs.addSelectedChangeListener(event -> {
//      channelName = event.getSelectedTab().getLabel();
//      list.setTopic("chat/" + channelName);
//      currentTopic = "chat/" + channelName;
//    });
//  }
//  
//  /*
//   * The Message Manager will not only persist chat logs to the database
//   * but it will also post the messages to the message list.
//   */
//  public void createMessageManagersWithPersisters() {
//     messageManagerGeneral = new MessageManager(this, userInfo, "chat/#general", new MessagePersister(messageService, messageRepository, userService));
//     messageManagerNotes = new MessageManager(this, userInfo, "chat/#notes", new MessagePersister(messageService, messageRepository, userService));
//     messageManagerIssues = new MessageManager(this, userInfo, "chat/#issues", new MessagePersister(messageService, messageRepository, userService));
//  }
//
//  @Override
//  public void beforeEnter(BeforeEnterEvent event) {
//    avatarGroup.setTopic("chat/" + channelName);
//  }
//
//  public void sendDiscordWebhookMessage(WebhookRepository webhookRepository, String username, String avatarURL,
//      String message) {
//    
//    String webhookURL = "";
//    
//    if (currentTopic.equals("chat/#general")) {
//      webhookURL = webhookRepository.findByWebhookName("Logs (Chat #general)").getWebhookUrl();
//    }
//    else if (currentTopic.equals("chat/#notes")) {
//      webhookURL = webhookRepository.findByWebhookName("Logs (Chat #notes)").getWebhookUrl();
//    }
//    else if (currentTopic.equals("chat/#issues")) {
//      webhookURL = webhookRepository.findByWebhookName("Logs (Chat #issues)").getWebhookUrl();
//    }
//    
//    // Trim leading & trailing whitespaces.
//    webhookURL = webhookURL.trim();
//    // Check for null or empty URL, if so - return, don't attempt to send.
//    if (webhookURL.isEmpty() || webhookURL == null || webhookURL.equals("") || webhookURL == "") {
//      System.out.println("URL is empty.");
//      return;
//    }
//    // Log output.
//    System.out.println("Attempting to send webhook message.");
//    System.out.println(webhookURL);
//    // Create & send webhook.
//    DiscordWebhook webhook = new DiscordWebhook(webhookURL);
//    webhook.setUsername(username);
//    webhook.setAvatarUrl(avatarURL);
//    webhook.setContent(message);
//    webhook.setTts(false);
//    try {
//      webhook.execute();
//    } catch (IOException e1) {
//      System.out.println(e1.toString());
//    }
//  }
//}