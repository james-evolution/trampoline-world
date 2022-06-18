package com.trampolineworld.views.contact;

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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.trampolineworld.views.*;
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
	
	private Label emailLabel, discordLabel;
	private Paragraph emailParagraph, discordParagraph, contactParagraph;
	
	private H2 header1 = new H2();
	private H2 emailHeader = new H2();
	private TextField emailSubject = new TextField();
	private TextArea emailMessageBody = new TextArea();
	private Button emailSendButton = new Button("Send");
	private VerticalLayout layout = new VerticalLayout();
	private HorizontalLayout contactRow1;



	public ContactView() {
		this.setWidthFull();

		addClassNames("userguide-view");
		setId("userguide-view");

		createLabelElements();
		createParagraphElements();
		createContactRows();

		header1.getElement().getStyle().set("margin-top", "18px !important");
		emailHeader.getElement().getStyle().set("margin-top", "8px !important");

		emailSendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		emailSendButton.addClickListener(e -> {
			String subject = emailSubject.getValue();
			String message = emailMessageBody.getValue();
			sendEmail("alkireson@gmail.com", subject, message);
		});

		// Create layout & add components to it.
		header1.setText("Questions or Requests?");
		layout.add(header1);
		layout.add(contactParagraph);
		layout.add(contactRow1);
		layout.add(emailHeader);
		layout.add(emailSubject);
		layout.add(emailMessageBody);
		layout.add(emailSendButton);

		this.setClassName("userguide-layout");

		// Add layout to the view.
		this.add(layout);
		
		this.setWidthFull();
		this.setHeightFull();
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
		contactRow1.add(emailLabel, emailParagraph, discordLabel, discordParagraph);

	}

	private void createParagraphElements() {
		// Create HTML paragraph elements.
		emailParagraph = new Paragraph("admin@evolutioncoding.net");
		discordParagraph = new Paragraph("James Z#0136");
		contactParagraph = new Paragraph(
				"Feel free to reach out to the developer through email or Discord. If you know his personal number, you may reach him there as well.");

		emailHeader.setText("Email the Developer");

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