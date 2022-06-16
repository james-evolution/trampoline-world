package com.trampolineworld.views.login;

import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;
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
@CssImport(
	    themeFor = "vaadin-login-overlay-wrapper vaadin-login-form-wrapper",
	    value = "./themes/trampolineworld/views/login-theme.css"
	)
public class LoginView extends LoginOverlay implements BeforeEnterObserver {
    public LoginView() {
        setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Trampoline World Sales & Repairs");
//        i18n.getHeader().setDescription("Login using user/user or admin/admin");
//        i18n.getHeader().setDescription("Login as 'user' or 'admin'");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
        
        addLoginListener(event -> {
        	TrampolineOrdersView.username = event.getUsername();
        });        
    }
    
	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		// inform the user about an authentication error
		if(beforeEnterEvent.getLocation()  
        .getQueryParameters()
        .getParameters()
        .containsKey("error")) {
            Notification.show("Login failed. Incorrect credentials.", 4000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
	}    

}
