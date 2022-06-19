package com.trampolineworld.views.account;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;

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
public class AccountView extends VerticalLayout implements BeforeEnterObserver {
	
	private final UserService userService;
	private final UserRepository userRepository;
	String currentUsername = "";
	
//	private VerticalLayout layout = new VerticalLayout();
	HorizontalLayout row1 = new HorizontalLayout();
	HorizontalLayout row2 = new HorizontalLayout();
	HorizontalLayout row3 = new HorizontalLayout();
	HorizontalLayout row4 = new HorizontalLayout();
	HorizontalLayout buttonRow = new HorizontalLayout();
	
	private H2 header1 = new H2();
	
	private Label accountNameLabel = new Label("Username:");
	private Label rolesLabel = new Label("Roles:");
	private Label currentPasswordLabel = new Label("Current Password:");
	private Label newPasswordLabel = new Label("New Password:");
	private Paragraph accountNameParagraph = new Paragraph();
	private Paragraph rolesParagraph = new Paragraph();
	
	private TextField newUsernameTextField = new TextField();
	private PasswordField oldPasswordTextField = new PasswordField();
	private PasswordField newPasswordTextField = new PasswordField();
	
	private Button saveUsernameButton = new Button("Save");
	private Button savePasswordButton = new Button("Save");


	public AccountView(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.setWidthFull();

		addClassNames("userguide-view");
		setId("userguide-view");
		
		// Get current user information.
		String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
		System.out.println("\n\n\n" + "USERNAME: " + currentUsername + "\n\n\n");
		User currentUser = userRepository.findByUsername(currentUsername);
		String currentHashedPassword = currentUser.getHashedPassword();
		Set<Role> roles = currentUser.getRoles();		
		
		// Configure rows.
		row1.setAlignItems(Alignment.BASELINE);
		row2.setAlignItems(Alignment.BASELINE);
		row3.setAlignItems(Alignment.BASELINE);
		row4.setAlignItems(Alignment.BASELINE);
		buttonRow.setAlignItems(Alignment.BASELINE);

		// Configure components.
		header1.setText("My Account");
		header1.getElement().getStyle().set("margin-top", "18px !important");
		accountNameParagraph.setText(currentUser.getName());
		rolesParagraph.setText(roles.toString());
		
//		newUsernameTextField.setPlaceholder("New Username");
//		oldPasswordTextField.setPlaceholder("Current Password");
//		newPasswordTextField.setPlaceholder("New Password");
		
		
		saveUsernameButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		savePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		configureLabels();
		
		
		// Populate rows.
		row1.add(accountNameLabel, accountNameParagraph, rolesLabel, rolesParagraph);
		row2.add(newUsernameTextField, saveUsernameButton);
		row3.add(currentPasswordLabel, oldPasswordTextField);
		row4.add(newPasswordLabel, newPasswordTextField);

		// Add components to layout.
		add(header1);
		add(row1);
		add(new H2("Change Username"));
		add(row2);
		add(new H2("Change Password"));
		add(row3);
		add(row4);
		add(savePasswordButton);

		// Configure layout.
		setClassName("userguide-layout");
		setWidthFull();
		setHeightFull();
		
		// Add button click listeners.
		saveUsernameButton.addClickListener(e -> {
			
			String desiredName = newUsernameTextField.getValue();
			currentUser.setName(desiredName);
			currentUser.setUsername(desiredName);
			
			try {
				// Update the user object in the database.
				userService.update(currentUser);
				// Notify of success & route user to login page.
				Notification.show("Account name changed. You have been logged out and will have to log in again.", 14000, Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(LoginView.class);
				// Notify user of error.
			} catch (Exception exception) {
				Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
		savePasswordButton.addClickListener(e -> {
//			String enteredHashedPassword = passwordEncoder.encode(oldPasswordTextField.getValue());
//			if (enteredHashedPassword == currentHashedPassword) {
			if (passwordEncoder.matches(oldPasswordTextField.getValue(), currentHashedPassword)) {
				String desiredHashedPassword = passwordEncoder.encode(newPasswordTextField.getValue());
				currentUser.setHashedPassword(desiredHashedPassword);
				
				try {
					userService.update(currentUser);
					// Notify of success & route user to login page.
					Notification.show("Password changed. You have been logged out and will have to log in again.", 14000, Position.TOP_CENTER)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					UI.getCurrent().navigate(LoginView.class);
					// Notify user of error.
				} catch (Exception exception) {
					Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
				}
			}
			else {
				Notification.show("Incorrect password.", 4000, Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
	}

	private void configureLabels() {
		// Create HTML label elements.
		accountNameLabel.addClassName("coloredLabel");
		rolesLabel.addClassName("coloredLabel");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub
		
	}
}