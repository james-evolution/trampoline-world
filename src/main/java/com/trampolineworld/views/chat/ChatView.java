package com.trampolineworld.views.chat;

import com.vaadin.collaborationengine.CustomMessageInput;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import javax.annotation.security.RolesAllowed;

@PageTitle("Chat Room")
@Route(value = "chat", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "USER" })
public class ChatView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

	private final UserService userService;
	private final UserRepository userRepository;
	private String channelName;
	CollaborationAvatarGroup avatarGroup;
	
	private static UserInfo loggedUserInfo;

	public ChatView(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;
		addClassName("chat-view");
		setSpacing(false);

		String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
		User currentUser = userRepository.findByUsername(currentUsername);

		// UserInfo is used by Collaboration Engine and is used to share details
		// of users to each other to able collaboration. Replace this with
		// information about the actual user that is logged, providing a user
		// identifier, and the user's real name. You can also provide the users
		// avatar by passing an url to the image as a third parameter, or by
		// configuring an `ImageProvider` to `avatarGroup`.
		UserInfo userInfo = new UserInfo(currentUser.getId().toString(), currentUser.getDisplayName());
		userInfo.setImage(currentUser.getProfilePictureUrl());
		userInfo.setColorIndex(currentUser.getColorIndex());

//		loggedUserInfo = new UserInfo(currentUser.getId().toString(), currentUser.getDisplayName());
//		loggedUserInfo.setImage(currentUser.getProfilePictureUrl());
//		loggedUserInfo.setColorIndex(currentUser.getColorIndex());

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
		CollaborationMessageList list = new CollaborationMessageList(userInfo, "chat/#general");
		list.setWidthFull();
		list.addClassNames("chat-view-message-list");

		// `CollaborationMessageInput is a textfield and button, to be able to
		// submit new messages. To avoid having to set the same info into both
		// the message list and message input, the input takes in the list as an
		// constructor argument to get the information from there.
//		CollaborationMessageInput input = new CollaborationMessageInput(list);
//		input.addClassNames("chat-view-message-input");
//		input.setWidthFull();

		// JAMES CUSTOM MESSAGE INPUT
		CustomMessageInput input = new CustomMessageInput(list, userInfo);
		input.addClassNames("chat-view-message-input");
		input.setWidthFull();
		
		// Add components to layout.
		add(avatarGroup, new Paragraph(), tabs, list, input);
		setSizeFull();
		expand(list);

		
		// Change the topic id of the chat when a new tab is selected
		tabs.addSelectedChangeListener(event -> {
			channelName = event.getSelectedTab().getLabel();
			list.setTopic("chat/" + channelName);
		});
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub
		avatarGroup.setTopic("chat/" + channelName);
	}

	// Need to test this. Ideally it'll remove the user's avatar when they navigate
	// elsewhere.
	@Override
	public void afterNavigation(AfterNavigationEvent event) {
//		String targetPath = event.getLocation().getPath();
//		if (targetPath != "chat") {
//			avatarGroup.setTopic("chat/" + channelName);
//			avatarGroup.setOwnAvatarVisible(false);
//		}
	}
}