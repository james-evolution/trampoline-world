package com.trampolineworld.views.login;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.TrampolineOrderService;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.debug.DebugView;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Login")
@Route(value = "login")
@CssImport(themeFor = "vaadin-login-overlay-wrapper vaadin-login-form-wrapper", value = "./themes/trampolineworld/views/login-theme.css")
@CssImport(themeFor = "vaadin-dialog-overlay", value = "./themes/trampolineworld/views/forgot-password.css")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

	@Autowired
	private JavaMailSender emailSender;
	private final UserService userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private Dialog forgotPasswordDialog = new Dialog();
	private Dialog resetPasswordDialog = new Dialog();
	private Dialog newPasswordDialog = new Dialog();
	private String resetCode;
	private User userLostPassword;

	public LoginView(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		setAction("login");

		this.userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;

		configureForgotPasswordDialog();
		configureResetCodeDialog();
		configureNewPasswordDialog();

		LoginI18n i18n = LoginI18n.createDefault();
		i18n.setHeader(new LoginI18n.Header());
		i18n.getHeader().setTitle("Trampoline World Sales & Repairs");
//        i18n.getHeader().setDescription("Login using user/user or admin/admin");
//        i18n.getHeader().setDescription("Login as 'user' or 'admin'");
		i18n.setAdditionalInformation(null);
		setI18n(i18n);

		setForgotPasswordButtonVisible(true);
		setOpened(true);

		this.addForgotPasswordListener(e -> {
			forgotPasswordDialog.open();
		});

		addLoginListener(event -> {
			// Get current username & object.
			String currentUsername = event.getUsername();
			User currentUser = userRepository.findByUsername(currentUsername);
			Set<Role> roles = currentUser.getRoles();
//			if (roles.contains(Role.TECH)) {
//				UI.getCurrent().navigate(DebugView.class);
//			}
		});
	}

	private void configureForgotPasswordDialog() {
		forgotPasswordDialog.setHeaderTitle("Forgot Password");
		forgotPasswordDialog.setDraggable(true);
		forgotPasswordDialog.addClassName("deleteDialog");
		// Create UI components.
		Paragraph confirmationMessage = new Paragraph(
				"Please enter your username so a reset code can be sent to your email. Make sure you check your spam folder once it's sent.");
		TextField fieldUsername = new TextField();
		fieldUsername.setHelperText("Enter your username here");
		// Create layout, add components to layout.
		VerticalLayout dialogLayout = new VerticalLayout(confirmationMessage, fieldUsername);
		dialogLayout.setPadding(false);
		dialogLayout.setSpacing(false);
		dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
		// Add layout to dialog.
		forgotPasswordDialog.add(dialogLayout);
		// Create buttons.
		Button sendResetLinkButton = new Button("Submit");
		sendResetLinkButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_PRIMARY);
		Button cancelButton = new Button("Cancel", e -> forgotPasswordDialog.close());
		// Configure click listened for send button.
		sendResetLinkButton.addClickListener(e -> {
			try {
				// Get user object from username. Get email from user object.
				String username = fieldUsername.getValue();
				userLostPassword = userRepository.findByUsername(username);
				String email = userLostPassword.getEmail();
				// Generate UUID.
				resetCode = UUID.randomUUID().toString();
				// Attempt to send email.
				sendEmail(email, "Reset Password", "Hello, " + username + ".\n\nYour reset code is: " + resetCode);
				// Notify of success.
				Notification.show("Reset link sent.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				// Open reset dialog.
				resetPasswordDialog.open();
				// Notify of failure.
			} catch (Exception exception) {
				Notification.show("Link failed to send.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
			forgotPasswordDialog.close();
		});
		// Add buttons to dialog.
		forgotPasswordDialog.getFooter().add(sendResetLinkButton);
		forgotPasswordDialog.getFooter().add(cancelButton);
	}

	private void configureResetCodeDialog() {
		resetPasswordDialog.setHeaderTitle("Reset Code");
		resetPasswordDialog.setDraggable(true);
		resetPasswordDialog.addClassName("deleteDialog");
		// Create UI components.
		Paragraph instructionsParagraph = new Paragraph(
				"Please enter the reset code that was sent to your email. It may be in your spam folder!");
		TextField inputResetCode = new TextField();
		inputResetCode.setHelperText("Enter your reset code here");
		// Create layout, add components to layout.
		VerticalLayout dialogLayout = new VerticalLayout(instructionsParagraph, inputResetCode);
		dialogLayout.setPadding(false);
		dialogLayout.setSpacing(false);
		dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
		// Add layout to dialog.
		resetPasswordDialog.add(dialogLayout);
		// Create buttons.
		Button submitResetCodeButton = new Button("Submit");
		submitResetCodeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_PRIMARY);
		Button cancelButton = new Button("Cancel", e -> resetPasswordDialog.close());
		// Configure click listened for send button.
		submitResetCodeButton.addClickListener(e -> {
			// Get user object from username. Get email from user object.
			String enteredResetCode = inputResetCode.getValue().trim(); // Trim leading & trailing whitespaces.
			if (enteredResetCode.equals(resetCode)) {
				// Notify of success.
				Notification.show("Reset code verified.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				resetPasswordDialog.close();
				newPasswordDialog.open();
			} else {
				Notification.show("Incorrect code.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		// Add buttons to dialog.
		resetPasswordDialog.getFooter().add(submitResetCodeButton);
		resetPasswordDialog.getFooter().add(cancelButton);
	}
	
	private void configureNewPasswordDialog() {
		newPasswordDialog.setHeaderTitle("New Password");
		newPasswordDialog.setDraggable(true);
		newPasswordDialog.addClassName("deleteDialog");
		// Create UI components.
		PasswordField inputNewPassword = new PasswordField();
		inputNewPassword.setHelperText("Enter a new password.");
		// Create layout, add components to layout.
		VerticalLayout dialogLayout = new VerticalLayout(inputNewPassword);
		dialogLayout.setPadding(false);
		dialogLayout.setSpacing(false);
		dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
		// Add layout to dialog.
		newPasswordDialog.add(dialogLayout);
		// Create buttons.
		Button submitNewPasswordButton = new Button("Submit");
		submitNewPasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_PRIMARY);
		Button cancelButton = new Button("Cancel", e -> newPasswordDialog.close());
		// Configure click listened for send button.
		submitNewPasswordButton.addClickListener(e -> {

			String newPassword = passwordEncoder.encode(inputNewPassword.getValue());
			
			try {
				// Encrypt the new password & save to database.
				userLostPassword.setHashedPassword(newPassword);
				userService.update(userLostPassword);
				// Notify of success.
				Notification.show("Reset code verified.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				newPasswordDialog.close();				
			}
			catch (Exception exception) {
				Notification.show("Incorrect code.", 4000, Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_ERROR);				
			}
		});

		// Add buttons to dialog.
		newPasswordDialog.getFooter().add(submitNewPasswordButton);
		newPasswordDialog.getFooter().add(cancelButton);
	}	

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		// Inform the user about an authentication error.
		if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
			this.setError(true);
		}
	}

	public void sendEmail(String recipient, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("james.evolution.1993@gmail.com");
		message.setTo(recipient);
		message.setSubject(subject);
		message.setText(text);
		emailSender.send(message);
	}
}
