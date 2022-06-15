package com.trampolineworld;

import com.vaadin.collaborationengine.CollaborationEngineConfiguration;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@NpmPackage(value = "@fontsource/karla", version = "4.5.0")
@Theme(value = "trampolineworld", variant = Lumo.DARK)
@PWA(
		name = "Trampoline World", 
		shortName = "TW", 
		description = "An application for tracking customer and order information.",
		themeColor = "#032e37",
		backgroundColor = "black",
		offlinePath = "offline.html",
		offlineResources = {"icons/icon.png", "images/offline.png"})
@Push
// 		offlineResources = {"images/logo.png", "images/offline.png"}
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CollaborationEngineConfiguration ceConfigBean() {
        CollaborationEngineConfiguration configuration = new CollaborationEngineConfiguration(
                licenseEvent -> {
                    // See <<ce.production.license-events>>
                });
        configuration.setDataDir("/license");
        return configuration;
    }

}
