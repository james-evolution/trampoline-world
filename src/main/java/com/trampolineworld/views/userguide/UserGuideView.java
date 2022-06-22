package com.trampolineworld.views.userguide;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
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
	private Label createLabel, editLabel, viewLabel, deleteLabel, fontSizeLabel, featuresLabel;
	private Paragraph createParagraph, editParagraph, viewParagraph, deleteParagraph, fontSizeParagraph;
	private Paragraph videoCaption;
	private UnorderedList featuresList;
	private VerticalLayout layout = new VerticalLayout();
	
	private Accordion accordionBasicsGuide = new Accordion();

	Tab documentationTab, desktopApplicationTab, mobileApplicationTab;
	private H2 installationHeader = new H2();
	private Paragraph installationDescription = new Paragraph();

	private IFrame videoFrame = new IFrame();
	private IFrame desktopInstallationFrame = new IFrame();
	private IFrame mobileInstallationFrame = new IFrame();

	public UserGuideView() {
		this.setWidthFull();

		addClassNames("userguide-view");
		setId("userguide-view");

		configureTabs();

		header1 = new H2("Application Overview");
		header1.getElement().getStyle().set("margin-top", "18px !important");
		header2 = new H2("The Basics: A Simple Guide");
		header2.getElement().getStyle().set("margin-top", "18px !important");

		videoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/134bgAV4l8k");
		videoFrame.getElement().setAttribute("title", "YouTube video player");
		videoFrame.getElement().setAttribute("frameborder", "0");
		videoFrame.getElement().setAttribute("allow", "accelerometer");
		videoFrame.getElement().setAttribute("autoplay", "true");
		videoFrame.getElement().setAttribute("allowfullscreen", "true");

		createLabelElements();
		createParagraphElements();

//		Details details = new Details("Application Features", featuresList);
//		details.setOpened(true);
//		details.addThemeVariants(DetailsVariant.FILLED);

		// Create layout & add components to it.
		layout.add(tabs);
		layout.add(header1);
		layout.add(videoFrame);
		layout.add(videoCaption);
		layout.add(new Hr());
		layout.add(featuresLabel);
		layout.add(featuresList);
//		layout.add(details);
		layout.add(new Hr());
		layout.add(header2);
		layout.add(createLabel, createParagraph);
		layout.add(editLabel, editParagraph);
		layout.add(viewLabel, viewParagraph);
		layout.add(deleteLabel, deleteParagraph);
		layout.add(fontSizeLabel, fontSizeParagraph);
		
		this.setClassName("userguide-layout");

		// Add layout to the view.
		this.add(layout);

		// Listen for tab changes, load layout components accordingly.
		tabs.addSelectedChangeListener(event -> {

			Optional<String> tabID = event.getSelectedTab().getId();

			if (tabID.equals(documentationTab.getId())) {
				loadDocumentationTab();
			} 
			else if (tabID.equals(desktopApplicationTab.getId())) {
				loadDesktopComponentData();
			} else if (tabID.equals(mobileApplicationTab.getId())) {
				loadMobileComponentData();
			}
		});
	}

	private void configureTabs() {
		documentationTab = new Tab("Overview");
		documentationTab.setId("documentationTab");
		desktopApplicationTab = new Tab("Desktop Application");
		desktopApplicationTab.setId("desktopApplicationTab");
		mobileApplicationTab = new Tab("Mobile Application");
		mobileApplicationTab.setId("mobileApplicationTab");
		tabs = new Tabs(documentationTab, desktopApplicationTab, mobileApplicationTab);
		tabs.setWidthFull();

		styleInstallationElements();
	}

	private void createParagraphElements() {
		// Create HTML paragraph elements.
		createParagraph = new Paragraph(
				"Simply click the New Order button at the top of the Trampoline Orders page : )");
		createParagraph.getElement().getStyle().set("margin-top", "0px !important");

		editParagraph = new Paragraph(
				"Left click on whichever row you wish to edit. A form will appear that is automatically populated with that particular order's data. "
						+ "\nYou can then edit the desired fields and finalize your changes by clicking the Save button at the bottom of the form.");
		editParagraph.getElement().getStyle().set("margin-top", "0px !important");

		viewParagraph = new Paragraph(
				"Right click the row and select View from the context menu. This will bring up a more detailed view of the order on a separate page. This is the best method of viewing orders with long descriptions.");
		viewParagraph.getElement().getStyle().set("margin-top", "0px !important");

		deleteParagraph = new Paragraph(
				"Right click the row and select Delete from the context menu. A confirmation dialog will appear and you'll have to click the Delete button again to confirm the action. This is to prevent accidental deletions.");
		deleteParagraph.getElement().getStyle().set("margin-top", "0px !important");

		fontSizeParagraph = new Paragraph(
				"To change the font size of the application, simply zoom in or out with Ctrl + or Ctrl - on the keyboard.");
		fontSizeParagraph.getElement().getStyle().set("margin-top", "0px !important");

		videoCaption = new Paragraph(
				"\nThe above video gives a comprehensive overview of this application's features, and will also teach you how to use it."
						+ "\nIn addition to this being a website, this system is also both a desktop and mobile application (compatible with macOS, Windows, iPhones, and Androids)");

		featuresList = new UnorderedList(
				new ListItem("Authentication & Authorization: Users must log in to access this application. Individual permissions depend upon account type."),
				new ListItem("CRUD Operations: Users can create, read, update, and delete orders."),
				new ListItem("Searching, Sorting, & Filtering: All pages with grid views have these features enabled. Admins can search through orders, system users, and system logs."),
				new ListItem("Column Reordering, Resizing, & Filtering: Users can drag columns to reorder and resize them. Additionally, they can hide/show specific columns with the Show/Hide button. (By default, on some pages, some columns are not shown, to minimize the odds of displaying unnecessary data.)"),
				new ListItem("Data Exports: Order information can be exported as needed in either PDF or CSV format."),
				new ListItem("Form Validation: The form used to create new orders and edit existing ones can validate input to meet any specified requirements."),
				new ListItem("Live Chat: A still-in-development and optional feature is live-chat. This is showcased in the video."),
				new ListItem("Persistent Data Storage: This application uses a MySQL database to store & retrieve order information."),
				new ListItem("Archives: When an order gets deleted from the system, it isn't actually deleted from the database. Instead, it is simply flagged so that it does not appear on the orders page. Admins can view deleted orders in the archives, where they can restore them if they desire."),
				new ListItem("User Management: Admins can manage user accounts and modify their names, passwords, and permissions."),
				new ListItem("Audit Log: All user actions are logged in the system. Administrators can see who made what changes at what time."),
				new ListItem("Profile Customization: Admins can customize their own profile picture, color, display name, and email address."),
				new ListItem("Password Resets: If a user forgets their password they can have a reset code sent to their email via the login page. Alternatively, administrators can change their password."),
				new ListItem("Tooltips & Helper Text: Some columns on the user management page show helpful hints/information if you hover over them with your cursor. Many input fields also have helper text beneath them as guidelines."),
				new ListItem("Open Source: The full source code used to develop this application is freely available to the owners of Trampoline World."),
				new ListItem("Questions & Requests: The contact page allows admins to instantly contact the developer if they have questions or want to make requests.")
			);
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
		featuresLabel = new Label("Application Features:");
		featuresLabel.addClassName("coloredLabel");
	}

	private void loadDocumentationTab() {
		// https://www.youtube.com/embed/-ZnYEI14B8c
		layout.removeAll();
		layout.add(tabs);
		header1.setText("Application Overview");
		layout.add(header1);
		layout.add(videoFrame);
		layout.add(videoCaption);
		layout.add(new Hr());
		layout.add(featuresLabel);
		layout.add(featuresList);
		layout.add(new Hr());
		layout.add(header2);
		layout.add(createLabel, createParagraph);
		layout.add(editLabel, editParagraph);
		layout.add(viewLabel, viewParagraph);
		layout.add(deleteLabel, deleteParagraph);
		layout.add(fontSizeLabel, fontSizeParagraph);
	}

	private void styleInstallationElements() {
		installationHeader.getElement().getStyle().set("margin-top", "18px !important");
		installationDescription.getElement().getStyle().set("margin-top", "16px !important");
	}

	private void loadDesktopComponentData() {
		installationHeader.setText("Installing the Desktop Application");
		installationDescription.setText(
				"Installing this system as a desktop application is quite an easy process! It should take under a minute.\n"
						+ "\nOn Windows, please make sure you're using the Google Chrome browser if you decide to do this, as not all browsers offer this capability."
						+ "\nOn Apple devices, you'll want to use Safari. It's possible that the icon may look like a plus sign instead of the one showcased in the video.\n");
		desktopInstallationFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/t65jdhNLmm8");
		desktopInstallationFrame.getElement().setAttribute("title", "YouTube video player");
		desktopInstallationFrame.getElement().setAttribute("frameborder", "0");
		desktopInstallationFrame.getElement().setAttribute("allow", "accelerometer");
		desktopInstallationFrame.getElement().setAttribute("autoplay", "true");
		desktopInstallationFrame.getElement().setAttribute("allowfullscreen", "true");

		// Clear layout and add components back into it.
		layout.removeAll();
		layout.add(tabs);
		layout.add(installationHeader);
		layout.add(desktopInstallationFrame);
		layout.add(installationDescription);
	}

	private void loadMobileComponentData() {
		installationHeader.setText("Installing the Mobile Application");
		installationDescription.setText(
				"The installation process for the mobile app is just as quick! The steps are relatively the same for both iPhone and Android devices.\n"
						+ "\niPhone users should use Safari." + "\nAndroid users should use Google Chrome.\n\n");
		mobileInstallationFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/9HWKx4NncvM");
		mobileInstallationFrame.getElement().setAttribute("title", "YouTube video player");
		mobileInstallationFrame.getElement().setAttribute("frameborder", "0");
		mobileInstallationFrame.getElement().setAttribute("allow", "accelerometer");
		mobileInstallationFrame.getElement().setAttribute("autoplay", "true");
		mobileInstallationFrame.getElement().setAttribute("allowfullscreen", "true");

		// Clear layout and add components back into it.
		layout.removeAll();
		layout.add(tabs);
		layout.add(installationHeader);
		layout.add(mobileInstallationFrame);
		layout.add(installationDescription);
	}
}