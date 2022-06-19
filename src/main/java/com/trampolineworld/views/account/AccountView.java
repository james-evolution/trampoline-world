package com.trampolineworld.views.account;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.FileData;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.*;
import com.trampolineworld.views.debug.DebugView;
import com.trampolineworld.views.login.LoginView;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;

@Route(value = "account", layout = MainLayout.class)
@PageTitle("Account")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@RolesAllowed("ADMIN")
public class AccountView extends HorizontalLayout implements BeforeEnterObserver {

	private final UserService userService;
	private final UserRepository userRepository;
	String currentUsername = "";

	private Image profilePicture;
	private Upload upload;
	private FileBuffer buffer;

	private H2 headerAccount = new H2();
	private H2 headerUsername = new H2("Change Username");
	private H2 headerEmail = new H2("Change Email");
	private H2 headerPassword = new H2("Change Password");

	VerticalLayout layout = new VerticalLayout();
	HorizontalLayout rowAccountName = new HorizontalLayout();
	HorizontalLayout rowAccountEmail = new HorizontalLayout();
	HorizontalLayout rowAccountRoles = new HorizontalLayout();
	HorizontalLayout rowChangeUsername = new HorizontalLayout();
	HorizontalLayout rowChangeEmail = new HorizontalLayout();
	HorizontalLayout rowCurrentPassword = new HorizontalLayout();
	HorizontalLayout rowNewPassword = new HorizontalLayout();
	HorizontalLayout buttonRow = new HorizontalLayout();

	private Label accountNameLabel = new Label("Username:");
	private Label emailLabel = new Label("Email:");
	private Label rolesLabel = new Label("Roles:");
	private Label currentPasswordLabel = new Label("Current Password:");
	private Label newPasswordLabel = new Label("New Password:");

	private Paragraph accountNameParagraph = new Paragraph();
	private Paragraph emailParagraph = new Paragraph();
	private Paragraph rolesParagraph = new Paragraph();

	private TextField newEmailTextField = new TextField();
	private TextField newUsernameTextField = new TextField();
	private PasswordField currentPasswordTextField = new PasswordField();
	private PasswordField newPasswordTextField = new PasswordField();

	private Button saveEmailButton = new Button("Save");
	private Button saveUsernameButton = new Button("Save");
	private Button savePasswordButton = new Button("Save");

	public AccountView(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.userRepository = userRepository;

		addClassNames("userguide-view");
		setId("userguide-view");

		// Get current user information.
		String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
		User currentUser = userRepository.findByUsername(currentUsername);
		String avatarImageUrl = currentUser.getProfilePictureUrl();
		String currentHashedPassword = currentUser.getHashedPassword();
		Set<Role> roles = currentUser.getRoles();

		// Display profile picture, add listener to allow users to change it.
		profilePicture = new Image(avatarImageUrl, "");
		profilePicture.setWidth("5%");

//		ContextMenu contextMenu = new ContextMenu();
//		contextMenu.setOpenOnClick(true);
//		contextMenu.setTarget(profilePicture);
//		contextMenu.addItem("Change Picture", event -> {});
		configureUploadButton();

		// Populate role components & configure rows.
		populateRoleComponents(roles);
		configureRows();

		// Configure components.
		headerAccount.setText(currentUsername);
		headerAccount.getElement().getStyle().set("margin-top", "18px !important");
		headerAccount.getElement().getStyle().set("margin-bottom", "0px !important");
		accountNameParagraph.setText(currentUser.getName());
		emailParagraph.setText(currentUser.getEmail());
		rolesParagraph.setText(roles.toString());

		styleButtons();
		configureHeaders();
		configureParagraphs();
		configureLabels();

		// Populate rows.
		rowAccountName.add(accountNameLabel, accountNameParagraph);
		rowAccountEmail.add(emailLabel, emailParagraph);
		rowChangeEmail.add(newEmailTextField, saveEmailButton);
		rowChangeUsername.add(newUsernameTextField, saveUsernameButton);
		rowCurrentPassword.add(currentPasswordLabel, currentPasswordTextField);
		rowNewPassword.add(newPasswordLabel, newPasswordTextField);

		// Add components to layout.
		layout.add(headerAccount);
//		layout.add(avatarMenuBar);
		layout.add(profilePicture);
		layout.add(upload);
//		layout.add(rowAccountName);
		layout.add(rowAccountEmail);
		layout.add(rowAccountRoles);
		layout.add(headerUsername);
		layout.add(rowChangeUsername);
		layout.add(headerEmail);
		layout.add(rowChangeEmail);
		layout.add(headerPassword);
		layout.add(rowCurrentPassword);
		layout.add(rowNewPassword);
		layout.add(savePasswordButton);
		layout.setClassName("userguide-layout");

		// Configure layout.
		this.add(layout);
		this.setClassName("userguide-layout");

		/*
		 * saveUsernameButton click listener
		 */
		saveUsernameButton.addClickListener(e -> {

			String desiredName = newUsernameTextField.getValue();
			currentUser.setName(desiredName);
			currentUser.setUsername(desiredName);

			try {
				// Update the user object in the database.
				userService.update(currentUser);
				// Notify of success & route user to login page.
				Notification.show("Account name changed. You have been logged out and will have to log in again.",
						14000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(LoginView.class);
				// Notify user of error.
			} catch (Exception exception) {
				Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});

		/*
		 * saveEmailButton click listener
		 */
		saveEmailButton.addClickListener(e -> {
			currentUser.setEmail(newEmailTextField.getValue());

			try {
				userService.update(currentUser);
				// Notify of success.
				Notification.show("Email address changed successfully.", 14000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				// Refresh page to update UI.
				UI.getCurrent().getPage().reload();
			} catch (Exception exception) {
				Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});

		/*
		 * savePasswordButton click listener
		 */
		savePasswordButton.addClickListener(e -> {
			if (passwordEncoder.matches(currentPasswordTextField.getValue(), currentHashedPassword)) {
				String desiredHashedPassword = passwordEncoder.encode(newPasswordTextField.getValue());
				currentUser.setHashedPassword(desiredHashedPassword);

				try {
					userService.update(currentUser);
					// Notify of success & route user to login page.
					Notification.show("Password changed. You have been logged out and will have to log in again.",
							14000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					UI.getCurrent().navigate(LoginView.class);
					// Notify user of error.
				} catch (Exception exception) {
					Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
							.addThemeVariants(NotificationVariant.LUMO_ERROR);
				}
			} else {
				Notification.show("Incorrect password.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
	}

	private void configureUploadButton() {
		buffer = new FileBuffer();
        upload = new Upload(buffer);
        upload.setAutoUpload(true);
        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", ".jpg", ".png");

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(
                    errorMessage,
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        
        upload.addSucceededListener(event -> {
            Notification notification = Notification.show(
                    "File uploaded successfully.",
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream();
            FileData fData = buffer.getFileData();
            File uploadedFile = fData.getFile();
            String absolutePath = uploadedFile.getAbsolutePath();
            System.out.println("\n\n--------------- UPLOADED FILE PATH ---------------");
            System.out.println(absolutePath);
            System.out.println("\n\n---------------- END OF FILE PATH ----------------");
        });
	}

	private void configureHeaders() {
		headerUsername.getElement().getStyle().set("margin-top", "18px");
		headerEmail.getElement().getStyle().set("margin-top", "18px");
		headerPassword.getElement().getStyle().set("margin-top", "18px");
	}

	private void configureParagraphs() {
		accountNameParagraph.getElement().getStyle().set("margin-bottom", "0px !important");
		emailParagraph.getElement().getStyle().set("margin-top", "0px !important");
		emailParagraph.getElement().getStyle().set("margin-bottom", "0px !important");
		rolesParagraph.getElement().getStyle().set("margin-top", "0px !important");
		rolesParagraph.getElement().getStyle().set("margin-bottom", "0px !important");
	}

	private void populateRoleComponents(Set<Role> roles) {
		if (roles.size() == 1) {
			rolesLabel.setText("Role:");
		} else if (roles.size() > 1) {
			rolesLabel.setText("Roles:");
		}

		rowAccountRoles.add(rolesLabel);

		for (Role r : roles) {
			String roleName = r.toString();
			Span badge = new Span(createIcon(roleName), new Span(roleName));
//	        Span badge = new Span(roleName);

			if (roleName == "ADMIN") {
				badge.getElement().getThemeList().add("badge success");
			} else if (roleName == "TECH") {
				badge.getElement().getThemeList().add("badge");
			} else if (roleName == "USER") {
				badge.getElement().getThemeList().add("badge contrast");
			}
			rowAccountRoles.add(badge);
		}
	}

	private Icon createIcon(String roleName) {
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

	private void styleButtons() {
		saveUsernameButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveEmailButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		savePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
	}

	private void configureRows() {
		rowAccountName.setAlignItems(Alignment.BASELINE);
		rowAccountEmail.setAlignItems(Alignment.BASELINE);
		rowAccountRoles.setAlignItems(Alignment.BASELINE);
		rowChangeEmail.setAlignItems(Alignment.BASELINE);
		rowChangeUsername.setAlignItems(Alignment.BASELINE);
		rowCurrentPassword.setAlignItems(Alignment.BASELINE);
		rowNewPassword.setAlignItems(Alignment.BASELINE);
		buttonRow.setAlignItems(Alignment.BASELINE);
	}

	private void configureLabels() {
		// Create HTML label elements.
		accountNameLabel.addClassName("coloredLabel");
		emailLabel.addClassName("coloredLabel");
		rolesLabel.addClassName("coloredLabel");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub

	}
}