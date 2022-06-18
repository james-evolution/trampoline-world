package com.trampolineworld.views.debug;

import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.security.RolesAllowed;

@PageTitle("Debug")
@Route(value = "debug", layout = MainLayout.class)
@RolesAllowed({ "TECH" })
public class DebugView extends VerticalLayout {

	private final UserService userService;
	private final UserRepository userRepository;
	private Button shellCommandButton = new Button("Execute Command");
	private TextField shellCommandTextField = new TextField();
	private HorizontalLayout shellHeaderContainer = new HorizontalLayout();
	private Paragraph consoleParagraph = new Paragraph();

	public DebugView(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;
		addClassName("chat-view");
		setSpacing(false);

		configureShellComponents();

		shellHeaderContainer.add(shellCommandTextField, shellCommandButton);

		add(shellHeaderContainer, consoleParagraph);
		setSizeFull();
	}

	/*
	 * Robert's method for executing shell scripts (modified)
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

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				consoleOutput.append("SHELL: " + line + "\n");
//				System.out.println("SHELL: " + line + "\n");
			}

			System.out.println(consoleOutput.toString());
			consoleParagraph.setText(consoleOutput.toString());
			System.out.println("\n\n----------  END OUTPUT  ----------\n\n");

//    		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
//    		Executors.newSingleThreadExecutor().submit(streamGobbler);

			int exitCode = process.waitFor();
		} catch (Exception e) {
			System.out.println(e);
			consoleOutput.append(e);
		}
	}

	private static class StreamGobbler implements Runnable {
		private InputStream inputStream;
		private Consumer<String> consumer;

		public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
		}

		public void run() {
		}
	}

	private void configureShellComponents() {
		// TODO Auto-generated method stub
		shellCommandTextField.setPlaceholder("Enter shell command...");

		shellCommandButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		shellCommandButton.addClickListener(e -> {
			String command = shellCommandTextField.getValue();
//			extractLicenseFromJar();
			if (command.isEmpty() || command == null) {
				executeShellScript(new String[] {
						"jar -xf target/trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json" });
			} else {
				executeShellScript(new String[] { command });
			}
		});
	}

}