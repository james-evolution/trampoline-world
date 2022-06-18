package com.trampolineworld.views.login;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.debug.DebugView;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;
import com.trampolineworld.views.trampolineordersreadonly.TrampolineOrdersReadOnlyView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Login")
@Route(value = "login")
@CssImport(themeFor = "vaadin-login-overlay-wrapper vaadin-login-form-wrapper", value = "./themes/trampolineworld/views/login-theme.css")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    @Autowired
    private JavaMailSender emailSender;
	private final UserService userService;
	private final UserRepository userRepository;

	public LoginView(UserService userService, UserRepository userRepository) {
		setAction("login");

		this.userService = userService;
		this.userRepository = userRepository;

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
//			Notification.show("An error has occurred, please contact the developer.", 4000, Position.TOP_CENTER);
			sendEmail("alkireson@gmail.com", "Forgot Password", "Test");
		});
		

		addLoginListener(event -> {
			// Get current username & object.
			String currentUsername = event.getUsername();
			User currentUser = userRepository.findByUsername(currentUsername);
			Set<Role> roles = currentUser.getRoles();
			// Check roles. If just a basic user and not an admin, forward to the page that
			// doesn't allow order deletion.
			if (roles.contains(Role.USER) && !roles.contains(Role.ADMIN)) {
				UI.getCurrent().navigate(TrampolineOrdersReadOnlyView.class);
			} else if (roles.contains(Role.TECH)) {
				UI.getCurrent().navigate(DebugView.class);
			}
		});
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
