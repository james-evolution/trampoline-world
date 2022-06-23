package com.trampolineworld.views.contact;

import com.trampolineworld.utilities.DiscordWebhook;
import com.trampolineworld.utilities.DiscordWebhook.EmbedObject;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;

import java.awt.Color;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.utilities.DiscordWebhook;
import com.trampolineworld.views.*;
import com.trampolineworld.views.archives.ArchivesView;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;

@Route(value = "contact", layout = MainLayout.class)
@PageTitle("Contact")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@RolesAllowed("ADMIN")
public class ContactView extends HorizontalLayout {

	@Autowired
	private JavaMailSender emailSender;
	private final String webhookURL = "https://ptb.discord.com/api/webhooks/988724055765033000/tSmaOypQVKtCkDBzpWCLWIF-drMcLKun0Otjd0Rrt79evjno_4Bb9bxkYP86nK5F2-SP";
	
	private final UserService userService;
	private final UserRepository userRepository;
	
	private Label emailLabel, discordLabel;
	private Paragraph emailParagraph, discordParagraph, contactParagraph, webhookCaptionParagraph;
	
	private H2 header1 = new H2();
	private H2 emailHeader = new H2();
	private H2 webhookHeader = new H2();
	private TextField emailSubject = new TextField();
	private TextArea emailMessageBody = new TextArea();
	private TextArea webhookMessageBody = new TextArea();
	
	private Button emailSendButton = new Button("Send");
	private Button webhookSendButton = new Button("Send");
	
	private VerticalLayout layout = new VerticalLayout();
	private HorizontalLayout contactRow1;
	private HorizontalLayout contactRow2;

	public ContactView(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;

		addClassNames("userguide-view");
		setId("userguide-view");

		createLabelElements();
		createParagraphElements();
		createContactRows();
		configureWebhookElements();

		header1.getElement().getStyle().set("margin-top", "18px !important");
		webhookHeader.getElement().getStyle().set("margin-top", "8px !important");
		emailHeader.getElement().getStyle().set("margin-top", "20px !important");

		emailSendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		emailSendButton.addClickListener(e -> {
			String subject = emailSubject.getValue();
			String message = emailMessageBody.getValue();
			sendEmail("admin@evolutioncoding.net", subject, message);
		});
		
		
		webhookSendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		webhookSendButton.addClickListener(e -> {
			// Get webhook message from text field.
			String webhookMessage = webhookMessageBody.getValue();
			// Create webhook object.
			DiscordWebhook webhook = new DiscordWebhook(webhookURL);
			// Get current user.
			String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
			User currentUser = userRepository.findByUsername(currentUsername);
			String avatarURL = currentUser.getProfilePictureUrl();
			/*
			 * To ping James Z#0136: <@178357031302987777>
			 * To ping the Developer role: <@&988212618059726870>
			 */
			// Configure Discord embed.
//			EmbedObject discordEmbed = new EmbedObject();
//			discordEmbed.setTitle("Message from TW");
//			discordEmbed.setDescription("<@&988212618059726870> " + webhookMessage);
//			discordEmbed.setColor(Color.CYAN);
//			discordEmbed.setAuthor(currentUsername, "https://trampolineworld.herokuapp.com/chat", avatarURL);
//			discordEmbed.setFooter(currentUsername, avatarURL);
			
			// Configure webhook message information.
			webhook.setUsername(currentUsername);
			webhook.setContent("<@&988212618059726870> " + webhookMessage);
			webhook.setAvatarUrl(avatarURL);
			webhook.setTts(true); // Text to speech.
			
//			webhook.addEmbed(discordEmbed);
			try {
				webhook.execute();
				Notification.show("Message sent successfully!", 4000, Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);				
			} catch (IOException e1) {
				e1.printStackTrace();
				Notification.show("Message failed to send!", 4000, Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_ERROR);				
			}
		});

		// Create layout & add components to it.
		header1.setText("Questions or Requests?");
		layout.add(header1);
		layout.add(contactParagraph);
		layout.add(contactRow2); // Discord
		layout.add(contactRow1); // Email
		layout.add(new Hr());
		layout.add(webhookHeader);
		layout.add(webhookCaptionParagraph);
		layout.add(webhookMessageBody);
		layout.add(webhookSendButton);
		layout.add(new Hr());
		layout.add(emailHeader);
		layout.add(emailSubject);
		layout.add(emailMessageBody);
		layout.add(emailSendButton);

		this.setClassName("userguide-layout");

		// Add layout to the view.
		this.add(layout);
		
		this.setWidthFull();
//		this.setHeightFull();
		
		// Reload page once to fix UI bugs.
		UI.getCurrent().navigate(ContactView.class);
	}

	private void configureWebhookElements() {
		
		webhookHeader.setText("Send a Discord Message");
		webhookHeader.getElement().getStyle().set("margin-top", "16px !important");
		webhookHeader.setWidth("100%");
		webhookCaptionParagraph = new Paragraph("This will tag the developer on Discord and he'll receive a notification alerting him that you're trying to contact him."
				+ "\nYour message will be read aloud via text-to-speech. Upon receiving this message, he'll join the Chat Room (as soon as possible) to respond.");
		webhookCaptionParagraph.getElement().getStyle().set("margin-top", "0px !important");
		webhookMessageBody.setPlaceholder("Enter a message...");
		webhookMessageBody.setWidth("100%");
		webhookMessageBody.setHeight("200px");
	}

	public void sendEmail(String recipient, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("james.evolution.1993@gmail.com");
		message.setTo(recipient);
		message.setSubject(subject);
		message.setText(text);
		try {
			emailSender.send(message);
			Notification.show("Email sent successfully!", 4000, Position.TOP_CENTER)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		} catch (Exception e) {
			Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
		}
	}

	private void createContactRows() {
		// Create contact rows.
		contactRow1 = new HorizontalLayout();
		contactRow1.setAlignItems(Alignment.BASELINE);
		contactRow1.add(emailLabel, emailParagraph);
		
		contactRow2 = new HorizontalLayout();
		contactRow2.setAlignItems(Alignment.BASELINE);
		contactRow2.add(discordLabel, discordParagraph);
	}

	private void createParagraphElements() {
		// Create HTML paragraph elements.
		emailParagraph = new Paragraph("admin@evolutioncoding.net");
		emailParagraph.getElement().getStyle().set("margin-top", "0px !important");
		
		discordParagraph = new Paragraph("James Z#0136");
		discordParagraph.getElement().getStyle().set("margin-bottom", "0px !important");
		
		contactParagraph = new Paragraph(
				"Feel free to reach out to the developer through email or Discord. If you know his personal number, you may reach him there as well."
				+ "\nAside from a phone call, Discord is the quickest way to reach him.");

		emailHeader.setText("Send an Email");

		emailSubject.setPlaceholder("Subject");
		emailSubject.setWidth("100%");

		emailMessageBody.setPlaceholder("Enter a message...");
		emailMessageBody.setWidth("100%");
		emailMessageBody.setHeight("200px");
	}

	private void createLabelElements() {
		// Create HTML label elements.
		emailLabel = new Label("Email:");
		emailLabel.addClassName("coloredLabel");
		discordLabel = new Label("Discord:");
		discordLabel.addClassName("coloredLabel");
	}
}