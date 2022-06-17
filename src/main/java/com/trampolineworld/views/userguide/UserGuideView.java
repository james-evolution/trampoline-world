package com.trampolineworld.views.userguide;

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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import com.trampolineworld.views.*;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;

@Route(value = "userguide", layout = MainLayout.class)
@PageTitle("User Guide")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@RolesAllowed("ADMIN")
public class UserGuideView extends HorizontalLayout {

	private Tabs tabs;
	private H2 header1, header2;
	private IFrame videoFrame = new IFrame();
	private Label createLabel, editLabel, viewLabel, deleteLabel, fontSizeLabel, emailLabel, discordLabel,
			featuresLabel;
	private Paragraph createParagraph, editParagraph, viewParagraph, deleteParagraph, fontSizeParagraph, emailParagraph,
			discordParagraph, contactParagraph;

	private Paragraph videoCaption;
	private UnorderedList featuresList;

	private Button goBackButton = new Button("Go Back");
	private VerticalLayout layout;
	private HorizontalLayout contactRow1, contactRow2;

	public UserGuideView() {
		addClassNames("userguide-view");
		setId("userguide-view");

		// Create tabs.
		Tab documentationTab = new Tab("Documentation");
		documentationTab.setId("documentationTab");
		Tab contactTab = new Tab("Support");
		contactTab.setId("contactTab");
		tabs = new Tabs(documentationTab, contactTab);
		tabs.setWidthFull();

		header1 = new H2("Application Overview");
		header2 = new H2("The Basics: A Simple Guide");

		videoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/134bgAV4l8k");
		videoFrame.getElement().setAttribute("title", "YouTube video player");
		videoFrame.getElement().setAttribute("frameborder", "0");
		videoFrame.getElement().setAttribute("allow", "accelerometer");
		videoFrame.getElement().setAttribute("autoplay", "true");
		videoFrame.getElement().setAttribute("allowfullscreen", "true");

		createLabelElements();
		createParagraphElements();
		createContactRows();

		// Configure button appearance and click listener.
		goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		goBackButton.addClickListener(e -> {
			UI.getCurrent().navigate(TrampolineOrdersView.class); // Send user back to the Trampoline Orders page.
		});

		// Create layout & add components to it.
		layout = new VerticalLayout();
		layout.add(tabs);
		layout.add(header1);
		layout.add(videoFrame);
		layout.add(videoCaption);
		layout.add(featuresLabel);
		layout.add(featuresList);
		layout.add(header2);
		layout.add(createLabel, createParagraph);
		layout.add(editLabel, editParagraph);
		layout.add(viewLabel, viewParagraph);
		layout.add(deleteLabel, deleteParagraph);
		layout.add(fontSizeLabel, fontSizeParagraph);
//        layout.add(goBackButton);
		this.setClassName("userguide-layout");

		// Add layout to the view.
		this.add(layout);
//        setSizeFull();;

		// Listen for tab changes, load layout components accordingly.
		tabs.addSelectedChangeListener(event -> {

			Optional<String> tabID = event.getSelectedTab().getId();

			if (tabID.equals(documentationTab.getId())) {
				loadDocumentationTab();
			} else if (tabID.equals(contactTab.getId())) {
				loadContactTab();
			}
		});
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
		createParagraph = new Paragraph(
				"Simply click the New Order button at the top of the Trampoline Orders page : )");
		editParagraph = new Paragraph(
				"Left click on whichever row you wish to edit. A form will appear that is automatically populated with that particular order's data. "
						+ "\nYou can then edit the desired fields and finalize your changes by clicking the Save button at the bottom of the form.");
		viewParagraph = new Paragraph(
				"Right click the row and select View from the context menu. This will bring up a more detailed view of the order on a separate page. This is the best method of viewing orders with long descriptions.");
		deleteParagraph = new Paragraph(
				"Right click the row and select Delete from the context menu. A confirmation dialog will appear and you'll have to click the Delete button again to confirm the action. This is to prevent accidental deletions.");
		fontSizeParagraph = new Paragraph(
				"To change the font size of the application, simply zoom in or out with Ctrl + or Ctrl - on the keyboard.");
		emailParagraph = new Paragraph("admin@evolutioncoding.net");
		discordParagraph = new Paragraph("James Z#0136");
		contactParagraph = new Paragraph(
				"Feel free to reach out to the developer through email or Discord. If you know his personal number, you may reach him there as well.");

		videoCaption = new Paragraph(
				"\nThe above video gives a comprehensive overview of this application's features, and will also teach you how to use it."
						+ "\nIn addition to this being a website, this system is also both a desktop and mobile application (compatible with macOS, Windows, iPhones, and Androids)");

		featuresList = new UnorderedList(new ListItem(
				"Authentication & Authorization: Users must log in to access this application. Individual permissions depend upon account type."),
				new ListItem("CRUD Operations: Users can create, read, update, and delete orders."),
				new ListItem(
						"Searching, Sorting, & Filtering: These features are available on the grid in which all orders are displayed."),
				new ListItem("Data Exports: Order information can be exported as needed in either PDF or CSV format."),
				new ListItem(
						"Form Validation: The form used to create new orders and edit existing ones can validate input to meet any specified requirements."),
				new ListItem(
						"Persistent Data Storage: This application uses a MySQL database to store & retrieve order information."),
				new ListItem(
						"Live Chat: A still-in-development and optional feature is live-chat. This is showcased in the video."),
				new ListItem(
						"Open Source: The full source code used to develop this application is freely available to the owners of Trampoline World."));
	}

	private void createLabelElements() {
		// Create HTML label elements.
		createLabel = new Label("Creating Orders:");
		createLabel.addClassName("coloredLabel");
		editLabel = new Label("Editing Orders:");
		editLabel.addClassName("coloredLabel");
		viewLabel = new Label("Viewing Orders:");
		viewLabel.addClassName("coloredLabel");
		deleteLabel = new Label("Deleting Orders:");
		deleteLabel.addClassName("coloredLabel");
		fontSizeLabel = new Label("Changing Font Size:");
		fontSizeLabel.addClassName("coloredLabel");
		emailLabel = new Label("Email:");
		emailLabel.addClassName("coloredLabel");
		discordLabel = new Label("Discord:");
		discordLabel.addClassName("coloredLabel");
		featuresLabel = new Label("Application Features:");
		featuresLabel.addClassName("coloredLabel");
	}

	private void loadContactTab() {
		layout.removeAll();
		layout.add(tabs);
		header1.setText("Need help?");
		layout.add(header1);
		layout.add(contactParagraph);
		layout.add(contactRow1, contactRow2);
//        layout.add(goBackButton);
	}

	private void loadDocumentationTab() {
		// https://www.youtube.com/embed/-ZnYEI14B8c
		layout.removeAll();
		layout.add(tabs);
		header1.setText("Application Overview");
		layout.add(header1);
		layout.add(videoFrame);
		layout.add(videoCaption);
		layout.add(featuresLabel);
		layout.add(featuresList);
		layout.add(header2);
		layout.add(createLabel, createParagraph);
		layout.add(editLabel, editParagraph);
		layout.add(viewLabel, viewParagraph);
		layout.add(deleteLabel, deleteParagraph);
		layout.add(fontSizeLabel, fontSizeParagraph);
//        layout.add(goBackButton);		
	}
}