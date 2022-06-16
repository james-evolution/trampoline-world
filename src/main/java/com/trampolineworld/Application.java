package com.trampolineworld;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.vaadin.collaborationengine.CollaborationEngineConfiguration;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

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

      //How to define path relative to current working directory 
        
//        File licensePath = new File("src/main/resources/META-INF/");
//        System.out.println(licensePath.getAbsolutePath());
       
//        String licensePath = getClass().getResource("ce-license.json").toString();
//        System.setProperty("vaadin.ce.dataDir", "./META-INF/resources");
        
        
//        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
//        String jarPath;
//		try {
//			jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
//			URL licensePath = new URL(jarPath + "/META-INF/resources");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//      String licensePath = "/app/target/trampolineworld-1.0-SNAPSHOT.jar!/BOOT-INF/classes!/META-INF/resources/ce-license.json"
        
        String licensePath = "/app/target/trampolineworld-1.0-SNAPSHOT.jar!/META-INF/resources/";
		configuration.setDataDir(licensePath);
        return configuration;
    }

}
