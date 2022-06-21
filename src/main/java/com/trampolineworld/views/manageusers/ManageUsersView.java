package com.trampolineworld.views.manageusers;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.utilities.DiscordWebhook;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@PageTitle("Manage Users")
@Route(value = "users/:userID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({ "ADMIN" })
@Uses(Icon.class)
@CssImport(themeFor = "vaadin-grid", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(value = "./themes/trampolineworld/views/dialog.css", themeFor = "vaadin-dialog-overlay")
public class ManageUsersView extends Div implements BeforeEnterObserver {

	private final String USER_ID = "userID";
	private final String USER_EDIT_ROUTE_TEMPLATE = "users/%s/edit";
	private final String USER_VIEW_ROUTE_TEMPLATE = "view_user/%s";
	private UUID targetId;
	
	private UserInfo userInfo;
	private User userToEdit;
	private boolean isNewUser = false;

	private Grid<User> grid = new Grid<>(User.class, false);
	H2 editTitle;
	private TextField filterTextField = new TextField();
	private TextField username, displayName, email, profilePictureUrl;
	private PasswordField hashedPassword;

	CheckboxGroup<Role> roles;
	Select<Integer> colorIndex;

	private GridContextMenu<User> menu;
	private Div editorLayoutDiv;
	private Div editorDiv;
	private FormLayout formLayout;
	private HorizontalLayout buttonHeaderContainer = new HorizontalLayout();

	private Dialog confirmDeleteDialog = new Dialog();

	private Button cancel = new Button("Cancel");
	private Button save = new Button("Save");
	private Button newUserButton = new Button("New User");
	private Button hideSidebarButton = new Button("Hide");

	private CollaborationBinder<User> binder;
	private User user;
	private final UserService userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public ManageUsersView(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		addClassNames("trampoline-orders-view");

		// Create split-view UI
		SplitLayout splitLayout = new SplitLayout();

		// Create grid and editor layouts.
		createGridLayout(splitLayout);
		createEditorLayout(splitLayout);

		// Create button header bar.
		createButtonHeader(splitLayout); // Requires splitLayout argument to define button functions.

		// Add buttonHeaderContainer and splitLayout to view.
		add(buttonHeaderContainer);
		add(splitLayout);

		// Configure the grid.
		configureGrid(userService, splitLayout);

		// Default user is new.
		userToEdit = new User();
		userToEdit.setHashedPassword(passwordEncoder.encode("user"));
		
		// Configure the form.
//		configureFormBindings(userInfo);
		configureFormButtons(userService);
	}

	private void configureFormButtons(UserService userService) {
		// When the cancel button is clicked, clear the form and refresh the grid.
		cancel.addClickListener(e -> {
			clearForm();
			updateGrid();
			editorLayoutDiv.setVisible(false);
			hideSidebarButton.setVisible(false);
		});

		// When the save button is clicked, save the new user.
		save.addClickListener(e -> {
			try {
				// Pull data from form, update user object, update repository.
				updateUserFromForm(userService);
				// Clear form, update grid.
				clearForm();
				updateGrid();
				// Hide editor & hide 'hide' button.
				editorLayoutDiv.setVisible(false);
				hideSidebarButton.setVisible(false);
				// Notify of success.
				Notification.show("User details stored.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(ManageUsersView.class);
				// Notify of failure.
			} catch (Exception exception) {
				Notification.show("Invalid form input.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
				System.err.println(exception.toString());
			}
		});
	}

	private void updateUserFromForm(UserService userService) {
		
		// Check if this is a new user, if it is, create a new user object & use the default encoded password.
		if (isNewUser) {
			User newUser = new User();
			newUser.setUsername(username.getValue());
			newUser.setDisplayName(displayName.getValue());
			newUser.setEmail(email.getValue());
			newUser.setRoles(roles.getValue());
			newUser.setColorIndex(colorIndex.getValue() == null ? 1 : colorIndex.getValue());
			newUser.setProfilePictureUrl(profilePictureUrl.getValue());
			newUser.setHashedPassword(passwordEncoder.encode("user"));
//			userService.update(newUser);
			userRepository.save(newUser);
			
			User savedUser = userRepository.findByUsername(username.getValue());
			sendDiscordWebhookMessage(newUser.getUsername() + ": " + savedUser.getId().toString(), "uuids");
		}
		else {
			this.user.setUsername(username.getValue());
			this.user.setDisplayName(displayName.getValue());
			this.user.setEmail(email.getValue());
			this.user.setRoles(roles.getValue());
			this.user.setColorIndex(colorIndex.getValue() == null ? 1 : colorIndex.getValue());
			this.user.setProfilePictureUrl(profilePictureUrl.getValue());
			// If password field is empty or null, do nothing. Leave it as is.
			if (hashedPassword.getValue().isEmpty() || hashedPassword.getValue() == null) {}
			// Else, encode the new password.
			else { this.user.setHashedPassword(passwordEncoder.encode(hashedPassword.getValue()));}
			userService.update(this.user);
		}
	}
	

	private void configureGrid(UserService userService, SplitLayout splitLayout) {
		grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

		// Add columns to the grid.
		grid.addColumn("username").setAutoWidth(true).setResizable(true);
		grid.addColumn("displayName").setAutoWidth(true).setResizable(true);
		grid.addColumn(createStatusComponentRenderer()).setHeader("Roles").setAutoWidth(true).setResizable(true);
		grid.addColumn("email").setAutoWidth(true).setResizable(true);
		grid.addColumn("profilePictureUrl").setWidth("300px").setResizable(true);
		grid.addColumn("colorIndex").setAutoWidth(true).setResizable(true);
		updateGrid();

		// When a row is selected or deselected, populate form.
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				// Set user to selected user.
				userToEdit = event.getValue();
				// Show layout & hide button.
				editorLayoutDiv.setVisible(true);
				hideSidebarButton.setVisible(true);
				splitLayout.setSplitterPosition(0);
				UI.getCurrent().navigate(String.format(USER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			} else {
				// Hide layout & clear form.
				editorLayoutDiv.setVisible(false);
				clearForm();
				UI.getCurrent().navigate(ManageUsersView.class);
			}
		});
	}

	private void updateGrid() {
		grid.setItems(userService.findAll(filterTextField.getValue()));
		
		// Log UUIDs so we can keep track of them for the CE license.
		List<User> users = userService.findAllNoFilter();
		for (User u : users) {
//			sendDiscordWebhookMessage(u.getUsername() + ": " + u.getId().toString(), "uuids");
		}
	}

	// Generating icons based upon role.
	private static Icon createIcon(String roleName) {
		Icon icon = VaadinIcon.USER.create();

		if (roleName == "ADMIN") {
			icon = VaadinIcon.USER_STAR.create();
		} else if (roleName == "TECH") {
			icon = VaadinIcon.DESKTOP.create();
		} else if (roleName == "USER") {
			icon = VaadinIcon.USER.create();
		}
		icon.getStyle().set("padding", "var(--lumo-space-xs");

		return icon;
	}

	// Logic for generating role badges.
	private static final SerializableBiConsumer<Span, User> statusComponentUpdater = (span, user) -> {
		Set<Role> roles = user.getRoles();
		for (Role r : roles) {
			String roleName = r.toString();
			Span badge = new Span(createIcon(roleName), new Span(roleName));
			badge.getElement().getStyle().set("margin", "3px");
			if (roleName == "ADMIN") {
				badge.getElement().getThemeList().add("badge success");
			} else if (roleName == "TECH") {
				badge.getElement().getThemeList().add("badge");
			} else if (roleName == "USER") {
				badge.getElement().getThemeList().add("badge contrast");
			}
			span.add(badge);
		}
	};

	// For rendering a more complex component in a grid cell.
	private static ComponentRenderer<Span, User> createStatusComponentRenderer() {
		return new ComponentRenderer<>(Span::new, statusComponentUpdater);
	}

	private void createButtonHeader(SplitLayout splitLayout) {
		// Configure button header container.
		buttonHeaderContainer.setSpacing(false);
		buttonHeaderContainer.setAlignItems(Alignment.BASELINE);

		
		newUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		newUserButton.getElement().getStyle().set("margin-left", "6px");
		newUserButton.getElement().getStyle().set("margin-right", "6px");
		newUserButton.addClickListener(e -> {
			// Clear form contents & update grid data.
			clearForm();
			updateGrid();
			// Show editor layout, show hide button.
			editorLayoutDiv.setVisible(true);
			hideSidebarButton.setVisible(true);
			// Full screen the editor.
			splitLayout.setSplitterPosition(0);
		});

		hideSidebarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		hideSidebarButton.getElement().getStyle().set("margin-left", "0px !important");
		hideSidebarButton.getElement().getStyle().set("margin-right", "6px");
		hideSidebarButton.setVisible(false);

		hideSidebarButton.addClickListener(e -> {
			clearForm();
			updateGrid();
			editorLayoutDiv.setVisible(false);
			hideSidebarButton.setVisible(false);
		});

		filterTextField.setPlaceholder("Search...");
		filterTextField.setHelperText("Filter by name or email");
		filterTextField.setClearButtonVisible(true);
		filterTextField.setValueChangeMode(ValueChangeMode.LAZY); // Don't hit database on every keystroke. Wait for
																	// user to finish typing.
		filterTextField.addValueChangeListener(e -> updateGrid());

		buttonHeaderContainer.add(filterTextField, newUserButton, hideSidebarButton);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		Optional<UUID> userID = event.getRouteParameters().get(USER_ID).map(UUID::fromString);
		if (userID.isPresent()) {
			Optional<User> userFromBackend = userService.get(userID.get());
			if (userFromBackend.isPresent()) {
				populateForm(userToEdit);
			} else {
				Notification.show(String.format("The requested user was not found, ID = %d", userID.get()), 4000,
						Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
				// When a row is selected but the data is no longer available, update the grid
				// component.
				updateGrid();
				event.forwardTo(ManageUsersView.class);
			}
		}
	}

	private void createEditorLayout(SplitLayout splitLayout) {
		editorLayoutDiv = new Div();
		editorDiv = new Div();
		formLayout = new FormLayout();

		editTitle = new H2("New User");
		editorLayoutDiv.setClassName("editor-layout");
		editorLayoutDiv.setVisible(false);

		editorDiv.setClassName("editor");
		editorLayoutDiv.add(editorDiv);

		username = new TextField("Username");
		username.setHelperText("Your username is the one you log in with. This is not to be confused with the display name, which is simply for aesthetics.");
		
		displayName = new TextField("Display Name");
		displayName.setHelperText("Your display name is essentially your nickname. It's what will be displayed to other users in the system. It's a good idea to have a different display name than username, that way people don't know what username to type if they attempt to log into your account.");
		
		email = new TextField("Email");
		email.setHelperText("An email is not required, but if you ever forget your password, this is where your reset link & code will be sent.");

		roles = new CheckboxGroup<>();
		roles.setLabel("Roles");
		roles.setItems(Role.ADMIN, Role.TECH, Role.USER);
		roles.setHelperText("Admins have all permissions but cannot see the debug page. Techs can see the debug page. Users can only access the orders page and the chat. They cannot delete orders, but they can add, edit, and view them.");

		colorIndex = new Select<>();
		colorIndex.setLabel("Color Index");
		colorIndex.setItems(0, 1, 2, 3, 4, 5, 6, 7);
		colorIndex.setHelperText("[0=None] [1=Pink] [2=Purple] [3=Green] [4=Orange] [5=Magenta] [6=Cyan] [7=Yellow]");

		hashedPassword = new PasswordField();
		hashedPassword.setLabel("Password");
		hashedPassword.setHelperText("To leave the password unchanged or default (default password for new accounts is 'user'), leave this field blank. WARNING: Do not change the value of this field if you don't want to change a user's password. It will get encrypted.");
		hashedPassword.setValueChangeMode(ValueChangeMode.LAZY);
//		hashedPassword.addValueChangeListener(e -> {
//			// If the password value changes, switch to password encoding rather than password preserving.
//			isEditingUser = true;
//			configureFormBindings(userInfo);
//		});

		profilePictureUrl = new TextField("Profile Picture URL");
		profilePictureUrl.setHelperText("File uploads are not yet supported for profile pictures. You can, however, pass in an image URL. Right click on an image from the net and select 'Copy Image Address' and then paste it here. The url path must end in .jpg or .png");
		
		Component[] fields = new Component[] { username, displayName, email, roles, hashedPassword, profilePictureUrl, colorIndex };

		formLayout.add(fields);		editorDiv.add(editTitle, formLayout);
		createButtonLayout(editorLayoutDiv);
		splitLayout.addToSecondary(editorLayoutDiv);
	}

	private void createButtonLayout(Div editorLayoutDiv) {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonLayout.add(save, cancel);
		editorLayoutDiv.add(buttonLayout);
	}

	private void createGridLayout(SplitLayout splitLayout) {
		Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		splitLayout.addToPrimary(wrapper);
		wrapper.add(grid);
	}

	private void clearForm() {
		populateForm(null);
	}

	private void populateForm(User value) {
		this.user = value;
		String topic = null;

		if (this.user != null && this.user.getId() != null) {
			topic = "user/" + this.user.getId();
			editTitle.setText("Edit User");
			isNewUser = false;
			// Populate form data here.
			username.setValue(value.getUsername() == null ? "" : value.getUsername());
			displayName.setValue(value.getDisplayName() == null ? "" : value.getDisplayName());
			email.setValue(value.getEmail() == null ? "" : value.getEmail());
			roles.setValue(value.getRoles());
			colorIndex.setValue(value.getColorIndex() == null ? 1 : value.getColorIndex());
			profilePictureUrl.setValue(value.getProfilePictureUrl() == null ? "" : value.getProfilePictureUrl());
		} else {
			editTitle.setText("New User");
			isNewUser = true;
			username.clear();
			displayName.clear();
			email.clear();
			roles.clear();
			colorIndex.clear();
			profilePictureUrl.clear();
		}
		
		
	}
	
    public static void sendDiscordWebhookMessage(String message, String channel) {
    	
    	String webhookURL = "";
    	String webhookUsername = "";
    	
    	if (channel == "debugging") {
    		webhookUsername = "TW Debugger";
    		webhookURL = "https://ptb.discord.com/api/webhooks/988568130093744218/xoLscoKMWCX3_7t63MESyA4FW3P_KSY6dlLB0hzYxbrqw6mTLlLsMXr7GlBbYd5rI3Ku";
    	}
    	else if (channel == "uuids") {
    		webhookUsername = "TW UUID Logger";
    		webhookURL = "https://ptb.discord.com/api/webhooks/988570358691016784/MWE8EIOOh7-Eohofs0Dp6Wu6DiyEmr91hUcUXBMnyt6t0esLraN7XPTc-fKTNpfOjjvW";
    	}
    
    	DiscordWebhook webhook = new DiscordWebhook(webhookURL);
    	webhook.setContent("<@&988212618059726870> " + message);
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