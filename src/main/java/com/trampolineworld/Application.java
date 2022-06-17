package com.trampolineworld;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {
	
    @Autowired
    private JavaMailSender emailSender;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CollaborationEngineConfiguration ceConfigBean() {
        CollaborationEngineConfiguration configuration = new CollaborationEngineConfiguration(
                licenseEvent -> {
                    // See <<ce.production.license-events>>
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
	        executeShellScript(new String[]{"jar -xf target/trampolineworld-1.0-SNAPSHOT.jar META-INF/resources/ce-license.json"});
	        executeShellScript(new String[]{"ls"});

         A separate method for attempting to execute shell commands:
    		extractLicenseFromJar();
         */
        
        /*
         * CONFIGURING THE LICENSE PATH ON HEROKU:
         * Two paths have been successful so far:
         * 
         * String licensePath = "target";
         * String licensePath = "/app/META-INF/resources/"; 
         */
        String licensePath = "/app/META-INF/resources/"; // This works.
		configuration.setDataDir(licensePath);
        return configuration;
    }
    
    /*
     * Robert's method for executing shell scripts (modified)
     */
    public void executeShellScript(final String[] command) {
    	boolean isWindows = false;

    	
    	StringBuilder consoleOutput = new StringBuilder();
    	
    	try {
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		List<String> cmds = new ArrayList<String>(Arrays.asList(command));
    		if (isWindows) {
    			cmds.add(0, "/c");
    			cmds.add(0, "cmd.exe");
    			System.out.println("Command to be executed: " + cmds);
    			consoleOutput.append("Command to be executed: " + cmds);
    			builder.command(cmds.toArray(new String[] {}));
    		} else {
    			cmds.add(0, "-c");
    			cmds.add(0, "sh");
    			System.out.println("Command to be executed: " + cmds);
    			consoleOutput.append("Command to be executed: " + cmds);
    			builder.command(cmds.toArray(new String[] {}));
    		}
    		builder.directory(new File("/app"));
    		Process process = builder.start();
    		
    		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
    		
    		
    		Executors.newSingleThreadExecutor().submit(streamGobbler);
    		int exitCode = process.waitFor();
    	} catch (Exception e ) {
    		System.out.println(e);
    		consoleOutput.append(e);
    	}
    	
    	sendEmail("james.evolution.1993@gmail.com", "Application: License Extraction", consoleOutput.toString());
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
			}
			int exitVal = process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        String consoleOutput = output.toString();
        sendEmail("james.evolution.1993@gmail.com", "Application: License Extraction", consoleOutput);
	}
	
	/*
	 * Method for sending e-mails.
	 */
    public void sendEmail(String recipient, String subject, String text) {
  	        SimpleMailMessage message = new SimpleMailMessage(); 
  	        message.setFrom("james.evolution.1996@gmail.com");
  	        message.setTo(recipient); 
  	        message.setSubject(subject); 
  	        message.setText(text);
  	        emailSender.send(message);
  	    }
}
