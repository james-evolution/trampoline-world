package com.trampolineworld.views.manageusers;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
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
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
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
	private Grid<User> grid = new Grid<>(User.class, false);
	CollaborationAvatarGroup avatarGroup;
	H2 editTitle;
	private TextField filterTextField = new TextField();
	private TextField username, email, profilePictureUrl;
	private PasswordField hashedPassword;
	
	CheckboxGroup<Role> roles;
	Select<Integer> colorIndex;

	private GridContextMenu<User> menu;
	private Div editorLayoutDiv;
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
	public ManageUsersView(UserService userService,
			UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		addClassNames("trampoline-orders-view");

		String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
		User currentUser = userRepository.findByUsername(currentUsername);

		// UserInfo is used by Collaboration Engine and is used to share details
		// of users to each other to able collaboration. Replace this with
		// information about the actual user that is logged, providing a user
		// identifier, and the user's real name. You can also provide the users
		// avatar by passing an url to the image as a third parameter, or by
		// configuring an `ImageProvider` to `avatarGroup`.
		UserInfo userInfo = new UserInfo(currentUser.getId().toString(), currentUser.getName());
		userInfo.setImage(currentUser.getProfilePictureUrl());
		userInfo.setColorIndex(currentUser.getColorIndex());

		// Create split-view UI
		SplitLayout splitLayout = new SplitLayout();

		// Configure avatar group (users / pfps)
		avatarGroup = new CollaborationAvatarGroup(userInfo, null);
		avatarGroup.getStyle().set("visibility", "visible");

		// Create grid and editor layouts.
		createGridLayout(splitLayout);
		createEditorLayout(splitLayout);

		// Create button header bar.
		createButtonHeader(splitLayout); // Requires splitLayout argument to define button functions.

		// Add buttonHeaderContainer and splitLayout to view.
		add(buttonHeaderContainer);
		add(splitLayout);

		
		/*
		 * DISABLING THIS FOR NOW.
		 * 
		 * Two reasons:
		 * 
		 * 1. User accounts have very little information in them, so a detailed view option isn't necessary right now.
		 * 2. Deleting users in general is a bad idea because another will almost inevitably be created.
		 * 
		 * This is an issue because we don't want to hit or surpass the 20 user / month CollaborationEngine limit.
		 * It's better to repurpose existing user accounts (changing username / password) than it is to generate a new id
		 * that add to the 20 / month quota.
		 * 
		 * Context menu may return later for things such as viewing an audit log of a particular user's actions in the system.
		 */
		// Create context menu.
//		createContextMenu(userService); // View & Delete buttons.

		// Configure the grid.
		configureGrid(userService, splitLayout);

		// Configure & add delete confirmation dialog.
		configureDeleteDialog(userService);
		add(confirmDeleteDialog);

		// Configure the form.
		configureForm(userInfo);
		configureFormButtons(userService);
	}

	private void configureDeleteDialog(UserService userService) {
		confirmDeleteDialog.setHeaderTitle("Delete User");
		confirmDeleteDialog.setDraggable(true);
		confirmDeleteDialog.addClassName("deleteDialog");

		VerticalLayout dialogLayout = createDialogLayout();
		confirmDeleteDialog.add(dialogLayout);

		Button dialogDeleteUserButton = createDialogDeleteUserButton(confirmDeleteDialog, userService);
		Button cancelButton = new Button("Cancel", e -> confirmDeleteDialog.close());
		confirmDeleteDialog.getFooter().add(dialogDeleteUserButton);
		confirmDeleteDialog.getFooter().add(cancelButton);
	}

	private void configureFormButtons(UserService userService) {
		// When the cancel button is clicked, clear the form and refresh the grid.
		cancel.addClickListener(e -> {
			clearForm();
//            refreshGrid();
			updateGrid();
			editorLayoutDiv.setVisible(false);
			hideSidebarButton.setVisible(false);
		});

		// When the save button is clicked, save the new order.
		save.addClickListener(e -> {
			try {
				if (this.user == null) {
					this.user = new User();
				}
				binder.writeBean(this.user);

				userService.update(this.user);
				clearForm();
				updateGrid();
				editorLayoutDiv.setVisible(false);
				hideSidebarButton.setVisible(false);
				Notification.show("Order details stored.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(ManageUsersView.class);
			} catch (ValidationException validationException) {
				Notification.show("Invalid form input.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
	}

	private void configureForm(UserInfo userInfo) {
		binder = new CollaborationBinder<>(User.class, userInfo);
		binder.forField(roles, Set.class, Role.class).bind("roles");
		binder.forField(username, String.class).bind("username");
		binder.forField(username, String.class).bind("name");
		binder.forField(hashedPassword, String.class).withConverter(new HashedPasswordConverter(userInfo)).bind("hashedPassword");
		binder.bindInstanceFields(this);
	}
	
	private class HashedPasswordConverter implements Converter<String, String> {
		
		User user;
		
		HashedPasswordConverter(UserInfo userInfo) {
			user = userRepository.findByUsername(userInfo.getName());
		}

		@Override
		public Result<String> convertToModel(String value, ValueContext context) {
			
			// If no password was entered into the form, keep the existing one.
			if (value.isEmpty() || value == null) {
				return Result.ok(user.getHashedPassword());
			}
			// Else, encrypt the new password.
			else {
				try {
					return Result.ok(passwordEncoder.encode(value));
				} catch (Exception e) {
					return Result.error("Password encryption failed.");
				}
			}
		}

		@Override
		public String convertToPresentation(String value, ValueContext context) {
			// If no password was entered into the form, keep the existing one.
			if (value.isEmpty() || value == null) {
				return user.getHashedPassword();
			}
			// Else, encrypt the new password.
			else {
				return passwordEncoder.encode(value);
			}
		}
		
	}

	private void configureGrid(UserService userService, SplitLayout splitLayout) {
		grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
//		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		
		// Add columns to the grid.
		grid.addColumn("username").setAutoWidth(true).setResizable(true);
		grid.addColumn("email").setAutoWidth(true).setResizable(true);
		grid.addColumn(createStatusComponentRenderer()).setHeader("Roles").setAutoWidth(true).setResizable(true);
		grid.addColumn("profilePictureUrl").setWidth("300px").setResizable(true);
		grid.addColumn("colorIndex").setAutoWidth(true).setResizable(true);
		updateGrid();

		// When a row is selected or deselected, populate form.
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				editorLayoutDiv.setVisible(true);
				hideSidebarButton.setVisible(true);
				splitLayout.setSplitterPosition(0);
				UI.getCurrent().navigate(String.format(USER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			} else {
				editorLayoutDiv.setVisible(false);
				clearForm();
				UI.getCurrent().navigate(ManageUsersView.class);
			}
		});
	}

	private void updateGrid() {
		grid.setItems(userService.findAll(filterTextField.getValue()));
	}
	
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

	private static ComponentRenderer<Span, User> createStatusComponentRenderer() {
	    return new ComponentRenderer<>(Span::new, statusComponentUpdater);
	}

	private void createContextMenu(UserService userService) {
		// Add the context menu to the grid.
		menu = grid.addContextMenu();

		// Listen for the event in which the context menu is opened, then save the id of
		// the user that was right clicked on.
		menu.addGridContextMenuOpenedListener(event -> {
			targetId = event.getItem().get().getId();
		});
		// Add menu items to the grid.
		menu.addItem("Delete", event -> {
			try {
				confirmDeleteDialog.open();
			} catch (Exception e) {
				Notification.show("An error has occurred, please contact the developer.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
	}

	private void createButtonHeader(SplitLayout splitLayout) {
		// Configure button header container.
		buttonHeaderContainer.setSpacing(false);
		buttonHeaderContainer.setAlignItems(Alignment.BASELINE);

		newUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		newUserButton.getElement().getStyle().set("margin-left", "6px");
		newUserButton.getElement().getStyle().set("margin-right", "6px");
		newUserButton.addClickListener(e -> {
			clearForm();
			updateGrid();
			editorLayoutDiv.setVisible(true);
			hideSidebarButton.setVisible(true);
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

		filterTextField.setPlaceholder("Filter by name...");
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
				populateForm(userFromBackend.get());
			} else {
				Notification
						.show(String.format("The requested user was not found, ID = %d",
								userID.get()), 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
				// When a row is selected but the data is no longer available, update the grid
				// component.
				updateGrid();
				event.forwardTo(ManageUsersView.class);
			}
		}
	}

	private static VerticalLayout createDialogLayout() {

		Paragraph confirmationMessage = new Paragraph("Are you sure you want to delete this order?");
		VerticalLayout dialogLayout = new VerticalLayout(confirmationMessage);
		dialogLayout.setPadding(false);
		dialogLayout.setSpacing(false);
//        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
		dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

		return dialogLayout;
	}

	private Button createDialogDeleteUserButton(Dialog dialog, UserService userService) {
		Button dialogDeleteButton = new Button("Delete", e -> {
			try {
				userService.delete(targetId);
				Notification.show("Order deleted.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			} catch (Exception exception) {
				Notification.show("Operation failed.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
			dialog.close();
			updateGrid();
		});
		dialogDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

		return dialogDeleteButton;
	}

	private void createEditorLayout(SplitLayout splitLayout) {
		editTitle = new H2("New Order");
		editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		editorLayoutDiv.setVisible(false);

		Div editorDiv = new Div();
		editorDiv.setClassName("editor");
		editorLayoutDiv.add(editorDiv);
		
//		grid.addColumn("username").setAutoWidth(true).setResizable(true);
//		grid.addColumn("email").setAutoWidth(true).setResizable(true);
//		grid.addColumn("roles").setAutoWidth(true).setResizable(true);
//		grid.addColumn("profilePictureUrl").setWidth("300px").setResizable(true);
//		grid.addColumn("colorIndex").setAutoWidth(true).setResizable(true);

		FormLayout formLayout = new FormLayout();
		username = new TextField("Username");
		email = new TextField("Email");
		
		roles = new CheckboxGroup<>();
		roles.setLabel("Roles");
		roles.setItems(Role.ADMIN, Role.USER, Role.TECH);

		colorIndex = new Select<>();
		colorIndex.setLabel("Color Index");
		colorIndex.setItems(0, 1, 2, 3, 4, 5, 6, 7);
		colorIndex.setHelperText("0: None, 1: Pink, 2: Purple, 3: Green, 4: Orange, 5: Magenta, 6: Cyan, 7: Yellow");
		colorIndex.setHelperText("[0=None] [1=Pink] [2=Purple] [3=Green] [4=Orange] [5=Magenta] [6=Cyan] [7=Yellow]");
//		roles.setValue(Role.USER);
		
		hashedPassword = new PasswordField();
		hashedPassword.setLabel("Password");
		hashedPassword.setHelperText("All passwords are encrypted into hashes after submission.");
		
		profilePictureUrl = new TextField("Profile Picture URL");
		Component[] fields = new Component[] { username, email, roles, hashedPassword, profilePictureUrl, colorIndex };

		formLayout.add(fields);
		editorDiv.add(avatarGroup, editTitle, formLayout);
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

	private void refreshGrid() {
		grid.select(null);
//        grid.getLazyDataView().refreshAll();
		grid.getGenericDataView().refreshAll();
	}

	private void clearForm() {
		populateForm(null);
	}

	private void populateForm(User value) {
		this.user = value;
		String topic = null;
		
		if (this.user != null && this.user.getId() != null) {
			topic = "user/" + this.user.getId();
//            avatarGroup.getStyle().set("visibility", "visible");
			editTitle.setText("Edit User");
		} else {
//            avatarGroup.getStyle().set("visibility", "hidden");
			editTitle.setText("New User");
		}
		binder.setTopic(topic, () -> this.user);
		avatarGroup.setTopic(topic);
	}
}