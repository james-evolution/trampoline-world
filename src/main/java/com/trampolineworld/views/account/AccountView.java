package com.trampolineworld.views.account;

import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.trampolineworld.views.login.LoginView;


@Route(value = "account", layout = MainLayout.class)
@PageTitle("Account")
@RolesAllowed("ADMIN")
public class AccountView extends HorizontalLayout implements BeforeEnterObserver {

	private final UserService userService;
	private final UserRepository userRepository;
	private String currentUsername = "";
	private User currentUser;
	
	private Avatar avatar = new Avatar();
	private Dialog changeProfilePictureDialog = new Dialog();
	private Upload upload;
	private FileBuffer buffer;

	private H2 headerAccount = new H2();
	private H2 headerProfilePicture = new H2("Profile Picture");
	private H2 headerUsername = new H2("Change Username");
	private H2 headerEmail = new H2("Change Email");
	private H2 headerPassword = new H2("Change Password");

	VerticalLayout layout = new VerticalLayout();
	HorizontalLayout rowAccountTitle = new HorizontalLayout();
	HorizontalLayout rowAccountName = new HorizontalLayout();
	HorizontalLayout rowChangeProfilePicture = new HorizontalLayout();
	HorizontalLayout rowAvatarColors = new HorizontalLayout();
	HorizontalLayout rowAccountEmail = new HorizontalLayout();
	HorizontalLayout rowAccountRoles = new HorizontalLayout();
	HorizontalLayout rowChangeUsername = new HorizontalLayout();
	HorizontalLayout rowChangeEmail = new HorizontalLayout();
	HorizontalLayout rowCurrentPassword = new HorizontalLayout();
	HorizontalLayout rowNewPassword = new HorizontalLayout();

	private Label labelProfileColor = new Label("Profile Color:");
	private Label labelAccountName = new Label("Username:");
	private Label labelEmail = new Label("Email:");
	private Label labelRoles = new Label("Roles:");
	private Label labelCurrentPassword = new Label("Old Password:");
	private Label labelNewPassword = new Label("New Password:");

	private Paragraph paragraphAccountName = new Paragraph();
	private Paragraph paragraphEmail = new Paragraph();
	private Paragraph paragraphRoles = new Paragraph();

	private TextField inputProfileUrl = new TextField();
	private TextField inputNewEmail = new TextField();
	private TextField inputNewUsername = new TextField();
	private PasswordField inputCurrentPassword = new PasswordField();
	private PasswordField inputNewPassword = new PasswordField();

	private Button buttonSaveProfileUrl = new Button("Save");
	private Button buttonSaveEmail = new Button("Save");
	private Button buttonSaveUsername = new Button("Save");
	private Button buttonSavePassword = new Button("Save Password");

	public AccountView(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.userRepository = userRepository;

		addClassNames("userguide-view");
		setId("userguide-view");

		// Get current user information.
		currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
		currentUser  = userRepository.findByUsername(currentUsername);
		String avatarImageUrl = currentUser.getProfilePictureUrl();
		String currentHashedPassword = currentUser.getHashedPassword();
		Set<Role> roles = currentUser.getRoles();

		// Configure avatar.
		avatar.setName(currentUsername);
		avatar.setImage(avatarImageUrl);
		avatar.setColorIndex(currentUser.getColorIndex());
		
		// Row to show avatar color previews.
		configureAvatarPreviews(currentUsername, avatarImageUrl);

		// Populate role components & configure rows.
		populateRoleComponents(roles);
		configureRows();

		// Configure components.
		headerAccount.setText(currentUsername);
		headerAccount.getElement().getStyle().set("margin-top", "18px !important");
		headerAccount.getElement().getStyle().set("margin-bottom", "0px !important");
		inputProfileUrl.setPlaceholder("Enter an image URL...");
		paragraphAccountName.setText(currentUser.getName());
		paragraphEmail.setText(currentUser.getEmail());
		paragraphRoles.setText(roles.toString());

		styleButtons();
		configureHeaders();
		configureParagraphs();
		configureLabels();

		// Populate rows.
		rowAccountTitle.add(avatar, headerAccount);
		rowChangeProfilePicture.add(inputProfileUrl, buttonSaveProfileUrl);
		rowAccountName.add(labelAccountName, paragraphAccountName);
		rowAccountEmail.add(labelEmail, paragraphEmail);
		rowChangeEmail.add(inputNewEmail, buttonSaveEmail);
		rowChangeUsername.add(inputNewUsername, buttonSaveUsername);
		rowCurrentPassword.add(labelCurrentPassword, inputCurrentPassword);
		rowNewPassword.add(labelNewPassword, inputNewPassword);

		// Add components to layout.
		layout.add(rowAccountTitle);
//		layout.add(upload);
		layout.add(rowAccountEmail);
		layout.add(rowAccountRoles);
		layout.add(headerProfilePicture);
		layout.add(rowChangeProfilePicture);
		layout.add(new Paragraph("Select one of the previews below to change your profile color. Changes are automatically applied."));
		layout.add(rowAvatarColors);
		layout.add(headerUsername);
		layout.add(rowChangeUsername);
		layout.add(headerEmail);
		layout.add(rowChangeEmail);
		layout.add(headerPassword);
		layout.add(rowCurrentPassword);
		layout.add(rowNewPassword);
		layout.add(buttonSavePassword);
		layout.setClassName("account-layout");

		// Configure layout.
		this.add(layout);
		this.setClassName("account-layout");

		/*
		 * buttonSaveProfileUrl click listener
		 */
		buttonSaveProfileUrl.addClickListener(e -> {
			currentUser.setProfilePictureUrl(inputProfileUrl.getValue());
			userService.update(currentUser);
			UI.getCurrent().getPage().reload();
		});
		
		/*
		 * buttonSaveUsername click listener
		 */
		buttonSaveUsername.addClickListener(e -> {

			String desiredName = inputNewUsername.getValue();
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
		 * buttonSaveEmail click listener
		 */
		buttonSaveEmail.addClickListener(e -> {
			currentUser.setEmail(inputNewEmail.getValue());

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
		 * buttonSavePassword click listener
		 */
		buttonSavePassword.addClickListener(e -> {
			if (passwordEncoder.matches(inputCurrentPassword.getValue(), currentHashedPassword)) {
				String desiredHashedPassword = passwordEncoder.encode(inputNewPassword.getValue());
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

	private void configureAvatarPreviews(String currentUsername, String avatarImageUrl) {
		Avatar avatarNone = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarPink = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarPurple = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarGreen = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarOrange = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarMagenta = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarBlue = new Avatar(currentUsername, avatarImageUrl);
		Avatar avatarYellow = new Avatar(currentUsername, avatarImageUrl);

		avatarNone.setColorIndex(7);
		avatarPink.setColorIndex(0);
		avatarPurple.setColorIndex(1);
		avatarGreen.setColorIndex(2);
		avatarOrange.setColorIndex(3);
		avatarMagenta.setColorIndex(4);
		avatarBlue.setColorIndex(5);
		avatarYellow.setColorIndex(6);

		avatarNone.getElement().addEventListener("click", e -> {avatar.setColorIndex(7); currentUser.setColorIndex(7); userService.update(currentUser);});
		avatarPink.getElement().addEventListener("click", e -> {avatar.setColorIndex(0); currentUser.setColorIndex(0); userService.update(currentUser);});
		avatarPurple.getElement().addEventListener("click", e -> {avatar.setColorIndex(1); currentUser.setColorIndex(1); userService.update(currentUser);});
		avatarGreen.getElement().addEventListener("click", e -> {avatar.setColorIndex(2); currentUser.setColorIndex(2); userService.update(currentUser);});
		avatarOrange.getElement().addEventListener("click", e -> {avatar.setColorIndex(3); currentUser.setColorIndex(3); userService.update(currentUser);});
		avatarMagenta.getElement().addEventListener("click", e -> {avatar.setColorIndex(4); currentUser.setColorIndex(4); userService.update(currentUser);});
		avatarBlue.getElement().addEventListener("click", e -> {avatar.setColorIndex(5); currentUser.setColorIndex(5); userService.update(currentUser);});
		avatarYellow.getElement().addEventListener("click", e -> {avatar.setColorIndex(6); currentUser.setColorIndex(6); userService.update(currentUser);});
		
//		rowAvatarColors.add(profileColorLabel, avatarNone, avatarPink, avatarPurple, avatarGreen, avatarOrange, avatarMagenta, avatarBlue, avatarYellow);
		rowAvatarColors.add(avatarNone, avatarPink, avatarPurple, avatarGreen, avatarOrange, avatarMagenta, avatarBlue, avatarYellow);
	}

/*
 * This method doesn't work as intended. It needs to be rewritten.
 */
//	private void configureUploadButton() {
//		buffer = new FileBuffer();
//        upload = new Upload(buffer);
//        upload.setAutoUpload(true);
//        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", ".jpg", ".png");
//
//        upload.addFileRejectedListener(event -> {
//            String errorMessage = event.getErrorMessage();
//
//            Notification notification = Notification.show(
//                    errorMessage,
//                    5000,
//                    Notification.Position.TOP_CENTER
//            );
//            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//        });
//        
//        upload.addSucceededListener(event -> {
//            Notification notification = Notification.show(
//                    "File uploaded successfully.",
//                    5000,
//                    Notification.Position.TOP_CENTER
//            );
//            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            
//            String fileName = event.getFileName();
//            InputStream inputStream = buffer.getInputStream();
//            FileData fData = buffer.getFileData();
//            File uploadedFile = fData.getFile();
//            String absolutePath = uploadedFile.getAbsolutePath();
//            
//            
//            System.out.println("\n\n--------------- UPLOADED FILE PATH ---------------");
//            System.out.println(absolutePath);
//            System.out.println("\n\n---------------- END OF FILE PATH ----------------");
//        });
//	}

	private void configureHeaders() {
		headerProfilePicture.getElement().getStyle().set("margin-top", "18px");
		headerProfilePicture.getElement().getStyle().set("margin-bottom", "0px");
		headerUsername.getElement().getStyle().set("margin-top", "18px");
		headerEmail.getElement().getStyle().set("margin-top", "18px");
		headerPassword.getElement().getStyle().set("margin-top", "18px");
	}

	private void configureParagraphs() {
		paragraphAccountName.getElement().getStyle().set("margin-bottom", "0px !important");
//		emailParagraph.getElement().getStyle().set("margin-top", "0px !important");
		paragraphEmail.getElement().getStyle().set("margin-bottom", "0px !important");
		paragraphRoles.getElement().getStyle().set("margin-top", "0px !important");
		paragraphRoles.getElement().getStyle().set("margin-bottom", "0px !important");
	}

	private void populateRoleComponents(Set<Role> roles) {
		if (roles.size() == 1) {
			labelRoles.setText("Role:");
		} else if (roles.size() > 1) {
			labelRoles.setText("Roles:");
		}

		rowAccountRoles.add(labelRoles);

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
		buttonSaveProfileUrl.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSaveUsername.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSaveEmail.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSavePassword.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
	}

	private void configureRows() {
		rowAccountTitle.setAlignItems(Alignment.CENTER);
		rowAccountName.setAlignItems(Alignment.BASELINE);
		rowAvatarColors.setAlignItems(Alignment.CENTER);
		rowAccountEmail.setAlignItems(Alignment.BASELINE);
		rowAccountRoles.setAlignItems(Alignment.BASELINE);
		rowChangeEmail.setAlignItems(Alignment.BASELINE);
		rowChangeUsername.setAlignItems(Alignment.BASELINE);
		rowCurrentPassword.setAlignItems(Alignment.BASELINE);
		rowNewPassword.setAlignItems(Alignment.BASELINE);
	}

	private void configureLabels() {
		// Create HTML label elements.
		labelProfileColor.addClassName("coloredLabel");
		labelAccountName.addClassName("coloredLabel");
		labelEmail.addClassName("coloredLabel");
		labelRoles.addClassName("coloredLabel");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub

	}
}