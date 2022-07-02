package com.trampolineworld.views.userguide;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import com.trampolineworld.data.entity.Feature;
import com.trampolineworld.views.*;
import com.trampolineworld.views.MainLayout.MenuItemInfo.LineAwesomeIcon;

@SuppressWarnings("serial")
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
  private VerticalLayout layout = new VerticalLayout();

  Tab documentationTab, desktopApplicationTab, mobileApplicationTab;
  private H2 installationHeader = new H2();
  private Paragraph installationDescription = new Paragraph();

  private IFrame videoFrame = new IFrame();
  private IFrame desktopInstallationFrame = new IFrame();
  private IFrame mobileInstallationFrame = new IFrame();

  VirtualList<Feature> featuresList = new VirtualList<>();

  public UserGuideView() {
    this.setWidthFull();

    addClassNames("userguide-view");
    setId("userguide-view");

    configureTabs();

    header1 = new H2("Application Overview");
    header1.getElement().getStyle().set("margin-top", "18px !important");
    header2 = new H2("The Basics: A Simple Guide");
    header2.getElement().getStyle().set("margin-top", "18px !important");

    videoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/aWSZ6CWmWPA");
    videoFrame.getElement().setAttribute("title", "YouTube video player");
    videoFrame.getElement().setAttribute("frameborder", "0");
    videoFrame.getElement().setAttribute("allow", "accelerometer");
    videoFrame.getElement().setAttribute("autoplay", "true");
    videoFrame.getElement().setAttribute("allowfullscreen", "true");

    createLabelElements();
    createParagraphElements();
    configureFeaturesList();

    // Create layout & add components to it.
    layout.add(tabs);
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

    this.setClassName("userguide-layout");

    // Add layout to the view.
    this.add(layout);

    // Listen for tab changes, load layout components accordingly.
    tabs.addSelectedChangeListener(event -> {

      Optional<String> tabID = event.getSelectedTab().getId();

      if (tabID.equals(documentationTab.getId())) {
        loadDocumentationTab();
      } else if (tabID.equals(desktopApplicationTab.getId())) {
        loadDesktopComponentData();
      } else if (tabID.equals(mobileApplicationTab.getId())) {
        loadMobileComponentData();
      }
    });
  }

  private ComponentRenderer<Component, Feature> featureRenderer = new ComponentRenderer<>(feature -> {
    HorizontalLayout cardLayout = new HorizontalLayout();
    cardLayout.setMargin(true);

    VerticalLayout infoLayout = new VerticalLayout();
    infoLayout.setSpacing(false);
    infoLayout.setPadding(false);
    infoLayout.getElement().appendChild(ElementFactory.createStrong(feature.getLabel()));
    infoLayout.add(new Div(new Text(feature.getDescription())));

    if (feature.getLabel().equals("Discord Integration")) {
      LineAwesomeIcon icon = new LineAwesomeIcon("lab la-discord");
      icon.getStyle().set("zoom", "150%");
      icon.getStyle().set("margin-right", "0px !important");
      cardLayout.add(icon, infoLayout);
    } else {
      Icon icon = feature.getIcon();
      cardLayout.add(icon, infoLayout);
    }
    return cardLayout;
  });

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
    createParagraph = new Paragraph("Simply click the New Order button at the top of the Trampoline Orders page : )");
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
        "\nAn in-depth overview & video tutorial on how to operate this application. Enjoy : )");
    videoCaption.getStyle().set("margin-top", "0px");
  }
  
  private void configureFeaturesList() {
    // Create collection of Feature objects.
    List<Feature> features = new ArrayList<>();
    // Login & Security
    Feature featureLogin = new Feature();
    featureLogin.setLabel("Login & Security");
    featureLogin.setDescription(
        "Users must log in to access this system. Passwords are hash-encrypted and all client-server requests are protected against CSRF and XSS attacks.");
    featureLogin.setIcon(new Icon(VaadinIcon.LOCK));
    // Mobile & Desktop
    Feature featureMobileDesktop = new Feature();
    featureMobileDesktop.setLabel("Mobile & Desktop");
    featureMobileDesktop.setDescription(
        "In addition to this being a website, this system is also both a desktop and mobile application (compatible with macOS, Windows, iPhones, and Androids)");
    featureMobileDesktop.setIcon(new Icon(VaadinIcon.MOBILE_BROWSER));
    // Order Management
    Feature featureOrderManagement = new Feature();
    featureOrderManagement.setLabel("Order Management");
    featureOrderManagement
        .setDescription("Users can create, edit, and delete orders. Non-admins cannot delete orders.");
    featureOrderManagement.setIcon(new Icon(VaadinIcon.TABLE));
    // User Management
    Feature featureUserManagement = new Feature();
    featureUserManagement.setLabel("User Management");
    featureUserManagement.setDescription(
        "Admins can manage user accounts and modify their names, passwords, and roles - which impact their permissions.");
    featureUserManagement.setIcon(new Icon(VaadinIcon.USERS));
    // Searching, Sorting, & Filtering
    Feature featureSearching = new Feature();
    featureSearching.setLabel("Searching, Sorting, & Filtering");
    featureSearching.setDescription(
        "All pages with grid views have these features enabled. Admins can search through orders, system users, and system logs in a variety of ways.");
    featureSearching.setIcon(new Icon(VaadinIcon.SEARCH));
    // Resizable, Draggable & Filterable Columns
    Feature featureColumns = new Feature();
    featureColumns.setLabel("Resizable, Draggable, & Filterable Columns");
    featureColumns.setDescription(
        "Users can drag columns to reorder and resize them. Additionally, they can hide/show specific columns with the Show/Hide button. (On pages with an abundance of detailed information, some columns are not shown by default, but users can opt in to see them with the aforementioned button.)");
    featureColumns.setIcon(new Icon(VaadinIcon.GRID_H));
    // Data Exports
    Feature featureExports = new Feature();
    featureExports.setLabel("Data Exports");
    featureExports
        .setDescription("A comprehensive list of all orders be exported whenever desired in either PDF or CSV format.");
    featureExports.setIcon(new Icon(VaadinIcon.DOWNLOAD));
    // Order Archives
    Feature featureArchives = new Feature();
    featureArchives.setLabel("Order Archives");
    featureArchives.setDescription(
        "When an order gets deleted from the system, it isn't actually deleted from the database. Instead, it is simply flagged so that it does not appear on the orders page. Admins can view deleted orders in the archives, where they can restore them if they desire.");
    featureArchives.setIcon(new Icon(VaadinIcon.ARCHIVES));
    // Audit Log
    Feature featureAuditLog = new Feature();
    featureAuditLog.setLabel("Audit Log");
    featureAuditLog.setDescription(
        "All user actions are logged in the system. Administrators can see who made what changes at what time.");
    featureAuditLog.setIcon(new Icon(VaadinIcon.OPEN_BOOK));
    // Live Chat
    Feature featureLiveChat = new Feature();
    featureLiveChat.setLabel("Live Chat Room");
    featureLiveChat.setDescription(
        "All users have access to a chat room in which they can communicate to each other via different devices and locations. They can also store notes here. The chat history is persisted to the database and should never be lost, even between server restarts.");
    featureLiveChat.setIcon(new Icon(VaadinIcon.CHAT));
    // Profile Customization
    Feature featureProfileCustomization = new Feature();
    featureProfileCustomization.setLabel("Profile Customization");
    featureProfileCustomization.setDescription(
        "Users can customize their own profile picture, color, display name, email address, and password. Profile pictures can be animated gifs!");
    featureProfileCustomization.setIcon(new Icon(VaadinIcon.USER));
    // Password Resets
    Feature featurePasswordResets = new Feature();
    featurePasswordResets.setLabel("Password Resets");
    featurePasswordResets.setDescription(
        "If a user forgets their password they can have a reset code sent to their email via the login page. Alternatively, administrators can change their password by hand.");
    featurePasswordResets.setIcon(new Icon(VaadinIcon.PASSWORD));
    // In-Application Database Configuration
    Feature featureDatabaseConfiguration = new Feature();
    featureDatabaseConfiguration.setLabel("In-Application Database Configuration");
    featureDatabaseConfiguration.setDescription(
        "This application uses a MySQL database on Bluehost to store & retrieve order information. However, administrators can point this application to a database of their own choosing via the database configuration page.");
    featureDatabaseConfiguration.setIcon(new Icon(VaadinIcon.DATABASE));
    // Date/Time Sorting
    Feature featureDateSorting = new Feature();
    featureDateSorting.setLabel("Date/Time Sorting");
    featureDateSorting.setDescription(
        "By default, all orders and logs are sorted by their date or timestamp, with the most recent entries showing at the top of the grid.");
    featureDateSorting.setIcon(new Icon(VaadinIcon.CALENDAR_CLOCK));
    // Form Validation
    Feature featureFormValidation = new Feature();
    featureFormValidation.setLabel("Form Validation");
    featureFormValidation.setDescription(
        "The forms used to create and edit new orders or users can validate input to meet any specified requirements.");
    featureFormValidation.setIcon(new Icon(VaadinIcon.FORM));
    // Tooltips & Helper Text
    Feature featureTooltips = new Feature();
    featureTooltips.setLabel("Tooltips & Helper Text");
    featureTooltips.setDescription(
        "Some columns on the user management page show helpful hints/information if you hover over them with your cursor. Many input fields also have helper text beneath them as guidelines.");
    featureTooltips.setIcon(new Icon(VaadinIcon.INFO_CIRCLE_O));
    // Discord Integration
    Feature featureDiscord = new Feature();
    featureDiscord.setLabel("Discord Integration");
    featureDiscord.setDescription(
        "Admins & Techs can use the Discord Integration page to configure, enable, or disable data logging to Discord. Two log categories exist: Audit & Chat. If desired, admins can edit webhook URLs to send this data to whichever Discord server channels they wish.");
    // Open Source
    Feature featureOpenSource = new Feature();
    featureOpenSource.setLabel("Open Source");
    featureOpenSource.setDescription(
        "The full source code used to develop this application is freely available to the current owners of Trampoline World, to be modified however they wish.");
    featureOpenSource.setIcon(new Icon(VaadinIcon.GIFT));
    // Questions & Requests
    Feature featureQuestions = new Feature();
    featureQuestions.setLabel("Developer Contact");
    featureQuestions.setDescription("Have any questions or requests? You can instantly reach the developer through the contact page via text-to-speech Discord message or email. If it's an emergency, he can join the live chat to help, or you may call him if you have his number.");
    featureQuestions.setIcon(new Icon(VaadinIcon.QUESTION_CIRCLE_O));

    // Add features to collection.
    features.add(featureLogin);
    features.add(featureMobileDesktop);
    features.add(featureOrderManagement);
    features.add(featureUserManagement);
    features.add(featureSearching);
    features.add(featureColumns);
    features.add(featureExports);
    features.add(featureArchives);
    features.add(featureAuditLog);
    features.add(featureLiveChat);
    features.add(featureProfileCustomization);
    features.add(featurePasswordResets);
    features.add(featureDatabaseConfiguration);
    features.add(featureDateSorting);
    features.add(featureFormValidation);
    features.add(featureTooltips);
    features.add(featureDiscord);
    features.add(featureOpenSource);
//    features.add(featureQuestions);

    // Set items & renderer.
    featuresList.setItems(features);
    featuresList.setRenderer(featureRenderer);
//    featuresList.setHeight("500px");
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
    configureFeaturesList();
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
    desktopInstallationFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/CP9fvTKluV8");
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
    mobileInstallationFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/ThWTqH3x2Eo");
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