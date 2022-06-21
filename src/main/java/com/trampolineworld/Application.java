package com.trampolineworld;

import com.trampolineworld.utilities.DiscordWebhook;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.trampolineworld.license.LicenseStorageImplementation;
import com.vaadin.collaborationengine.CollaborationEngine;
import com.vaadin.collaborationengine.CollaborationEngineConfiguration;
import com.vaadin.collaborationengine.LicenseEventHandler;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
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
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {
	
    @Autowired
    private static JavaMailSender emailSender;
    private final static String webhookURL = "https://ptb.discord.com/api/webhooks/988366724682379294/g20NbSzfeL_QrZhZVWt-2rJh4I6MmSU_FtkPNv-9qeYq1MHbs5TKsv1g2NkMq8TLYT9o";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CollaborationEngineConfiguration ceConfigBean() {
        CollaborationEngineConfiguration configuration = new CollaborationEngineConfiguration(
                licenseEvent -> {
                    // See <<ce.production.license-events>>
                    switch (licenseEvent.getType()) {
                    case GRACE_PERIOD_STARTED:
                    case LICENSE_EXPIRES_SOON:
                    	sendDiscordWebhookMessage(licenseEvent.getMessage());
                        break;
                    case GRACE_PERIOD_ENDED:
                    case LICENSE_EXPIRED:
                    	sendDiscordWebhookMessage(licenseEvent.getMessage());
                        break;
                    }
                    sendDiscordWebhookMessage("Vaadin Collaboration Engine license needs to be updated");
                    sendDiscordWebhookMessage(licenseEvent.getMessage());
                    sendEmail("admin@evolutioncoding.net","Vaadin Collaboration Engine license needs to be updated",
                            licenseEvent.getMessage());                	
                });
        

        /*
         * CREATING / EXTRACTING THE LICENSE FILE ON HEROKU:
         * 
         * Vaadin's setDataDir() method seems to reject file paths that navigate into a jar file to find ce-license.json
         * Unfortunately, when deploying, Heroku only allows us to place the license file in the jar.
         * 
         * A workaround is to create the license file in either the /app or app/target folder via linux shell.
         * Alternatively, you can extract the license file from the jar and place it in one of those two folders.
         * 
         * Wherever you place create or place it, you'll have to reference it with setDataDir()
         * 
         * The following commands were attempts to do this programmatically, however, it seems that Heroku prevents the application from generating files.
         * These commands work when manually entered into a shell by hand, but not when done via code. Perhaps it's due to lacking write permissions.

	        executeShellScript(new String[]{"cd target", "jar -xf trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json"});
	        executeShellScript(new String[]{"cd target && jar -xf trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json"});
	        executeShellScript(new String[]{"cd target ; jar -xf trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json >> output.txt"});
	        executeShellScript(new String[]{"ls"});

			EDIT: The working solution is below. Use echo in cohesion with a redirection operator to redirect echo output (the license json information) to a new license file.
         */

        executeShellScript(new String[]{"echo '{\"content\":{\"key\":\"8b91663f-e7bc-45b2-9e01-0ea1ac1474ce\",\"owner\":\"Vaadin Core License\",\"quota\":20,\"endDate\":\"2100-01-01\"},\"checksum\":\"Q1sl676t10ofL0x23/TTwXrHK6Sc1VRujTetRpEBF4I=\"}' > ce-license.json"});
        String licensePath = "/app"; // This works.
		configuration.setDataDir(licensePath);
		
		return configuration;
    }
    
    /*
     * Robert's & Jeremy's method for executing shell scripts
     */
    public void executeShellScript(final String[] command) {
    	
    	StringBuilder consoleOutput = new StringBuilder();
    	
    	try {
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		List<String> cmds = new ArrayList<String>(Arrays.asList(command));
			cmds.add(0, "-c");
			cmds.add(0, "sh");
			
//			consoleOutput.append("SHELL: Command to be executed: " + cmds);
			
			System.out.println("----------------------------------");
			System.out.println("COMMAND TO BE EXECUTED: \n" + cmds.get(2));
			System.out.println("---------- SHELL OUTPUT ----------\n\n");
			
			builder.command(cmds.toArray(new String[] {}));
    		builder.directory(new File("/app"));
    		Process process = builder.start();
    		
			BufferedReader reader = new BufferedReader(new InputStreamReader (process.getInputStream()));
			String line;
			while((line = reader.readLine()) != null) {
				consoleOutput.append("SHELL: " + line + "\n");
//				System.out.println("SHELL: " + line + "\n");
			}    		
			
			System.out.println(consoleOutput.toString());
			System.out.println("\n\n----------  END OUTPUT  ----------\n\n");
    		
//    		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
//    		Executors.newSingleThreadExecutor().submit(streamGobbler);
    		
    		int exitCode = process.waitFor();
    	} catch (Exception e ) {
    		System.out.println(e);
    		consoleOutput.append(e);
    	}
    }
    
    private static class StreamGobbler implements Runnable {
    	private InputStream inputStream;
    	private Consumer<String> consumer;
    	
    	public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {}
    	public void run() {}
    }

    /*
     * Jeremy's method for executing shell scripts & routing console output to email.
     */
	private void extractLicenseFromJar() {
		StringBuilder output = new StringBuilder(); 
        try {
        	/*
        	 * A variety of attempts:
	        	Process process = Runtime.getRuntime().exec(new String[]{"cd target", "jar -xf trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json"}); 
				Process process = Runtime.getRuntime().exec("/app/.jdk/bin/jar -xf target/trampolineworld-1.0-SNAPSHOT.jar META-INF/resources/ce-license.json");
				Process process = Runtime.getRuntime().exec("/app/.jdk/bin/jar");
	        	String key = "{\"content\":{\"key\":\"8b91663f-e7bc-45b2-9e01-0ea1ac1474ce\",\"owner\":\"Vaadin Core License\",\"quota\":20,\"endDate\":\"2100-01-01\"},\"checksum\":\"Q1sl676t10ofL0x23/TTwXrHK6Sc1VRujTetRpEBF4I=\"}";
				Process process = Runtime.getRuntime().exec("echo \"" + key + "\" > target/ce-license.json");
				Process process = Runtime.getRuntime().exec("echo 'test' > target/ce-license.json"); // This works if manually typed only.
				Process process = Runtime.getRuntime().exec("umask");
        	 */
			Process process = Runtime.getRuntime().exec("/app/.jdk/bin/jar -xf target/trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader (process.getInputStream()));
			String line;
			while((line = reader.readLine()) != null) {
				output.append(line + "\n");
				System.out.println(line + "\n");
			}
			int exitVal = process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        String consoleOutput = output.toString();
        System.out.println(consoleOutput);
        
//        sendEmail("james.evolution.1993@gmail.com", "Application: License Extraction", consoleOutput);
	}
	
	/*
	 * Method for sending e-mails.
	 */
    public static void sendEmail(String recipient, String subject, String text) {
  	        SimpleMailMessage message = new SimpleMailMessage(); 
  	        message.setFrom("james.evolution.1996@gmail.com");
  	        message.setTo(recipient); 
  	        message.setSubject(subject); 
  	        message.setText(text);
  	        emailSender.send(message);
  	    }
    
    public static void sendDiscordWebhookMessage(String message) {
    	DiscordWebhook webhook = new DiscordWebhook(webhookURL);
    	webhook.setUsername("TW License Event Handler");
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
