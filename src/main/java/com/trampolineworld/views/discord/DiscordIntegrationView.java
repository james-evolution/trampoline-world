package com.trampolineworld.views.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;

import com.trampolineworld.data.entity.Webhook;
import com.trampolineworld.data.service.WebhookRepository;
import com.trampolineworld.data.service.WebhookService;
import com.trampolineworld.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

@PageTitle("Discord Integration Settings")
@Route(value = "discord", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "TECH" })
@Uses(Icon.class)
@CssImport(themeFor = "vaadin-grid", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-scroller", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(value = "./themes/trampolineworld/views/dialog.css", themeFor = "vaadin-dialog-overlay")
public class DiscordIntegrationView extends Div {

  private WebhookRepository webhookRepository;
  private WebhookService webhookService;
  private Webhook currentWebhook;
  private Grid<Webhook> grid;
  private IFrame videoFrame = new IFrame();
  private H2 titleDiscordIntegration = new H2("Discord Integration Settings");
  private H2 titleCreatingWebhooks = new H2("Creating Webhooks");
  private H2 titleWebhooks = new H2("Webhooks");
  private H2 titleWebhookDescriptions = new H2("Webhook Descriptions");
  private Button resetToDefaultsButton = new Button("Reset to Defaults");

  @Autowired
  public DiscordIntegrationView(WebhookRepository webhookRepository, WebhookService webhookService) {
    addClassNames("trampoline-orders-view");
    this.webhookRepository = webhookRepository;
    this.webhookService = webhookService;
    this.setWidthFull();

    HorizontalLayout headerRow = new HorizontalLayout();
    VerticalLayout verticalLayout = new VerticalLayout();

    HorizontalLayout descriptionRow = new HorizontalLayout();
    descriptionRow.setAlignItems(Alignment.BASELINE);

    Paragraph descriptionParagraph = new Paragraph(
        "An optional feature of this system is the ability to log certain data to Discord. Webhooks are the method by which we send that information."
            + "\nEvery time a webhook is created, it generates a URL. The webhook URL is what tells the data which channel to go to."
            + "\n\nTo disable logging, simply edit the webhook URL and save it as an empty field."
            + "\n\nAlternatively, if you wish to customize where this data is sent (such as ensuring it's sent to your own Discord server), you can quickly and easily create webhooks of your own."
            + " Then, simply replace these URLs with yours, and the data will be routed there in the future. A video guide on that process is below. (It's easier than it sounds!)"
            + "\n\n(Estimated Setup Time: 2-5 minutes)");

    videoFrame.getElement().setAttribute("src", "https://www.youtube.com/embed/134bgAV4l8k");
    videoFrame.getElement().setAttribute("title", "YouTube video player");
    videoFrame.getElement().setAttribute("frameborder", "0");
    videoFrame.getElement().setAttribute("allow", "accelerometer");
    videoFrame.getElement().setAttribute("autoplay", "true");
    videoFrame.getElement().setAttribute("allowfullscreen", "true");

    UnorderedList webhookDescriptionsList = new UnorderedList(
//        new ListItem("CE License Events: This is where license alerts are sent. In the event that our CollaborationEngine license needs to be renewed or upgraded, we'll be notified here."),
        new ListItem(
            "Logs (Audit): Just as every user action is logged in a database and displayed on the Audit Log page, it's also possible to have them logged to Discord, so we have a backup. This webhook specifies where those backup logs will be stored."),
        new ListItem("Logs (Chat #general): Logs from the #general chat channel are sent via this webhook."),
        new ListItem("Logs (Chat #notes): Logs from the #notes chat channel are sent via this webhook."),
        new ListItem("Logs (Chat #issues): Logs from the #issues chat channel are sent via this webhook."),
        new ListItem(
            "Logs (UUIDs): This is where we log the universally unique identifiers (uuids) of new users. It's important to store these UIIDs so we don't lose them."));

    titleDiscordIntegration.getStyle().set("margin-top", "8px !important");
    titleDiscordIntegration.getStyle().set("margin-bottom", "0px !important");
    descriptionParagraph.getStyle().set("margin-bottom", "0px !important");
    titleCreatingWebhooks.getStyle().set("margin-top", "8px !important");

    titleWebhooks.getStyle().set("margin-top", "8px !important");

    titleWebhookDescriptions.getStyle().set("margin-top", "8px !important");
    titleWebhookDescriptions.getStyle().set("margin-bottom", "0px !important");
    webhookDescriptionsList.getStyle().set("margin-top", "0px !important");

    verticalLayout.add(titleDiscordIntegration, descriptionParagraph, new Hr(), titleCreatingWebhooks, videoFrame,
        new Hr());

    // Create grid & editor.
    grid = new Grid<>(Webhook.class, false);
    grid.setColumnReorderingAllowed(true);
    grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    grid.addThemeVariants(GridVariant.LUMO_COMPACT);
    Editor<Webhook> editor = grid.getEditor();

    // Create columns.
    Grid.Column<Webhook> webhookNameColumn = grid.addColumn(Webhook::getWebhookName).setHeader("Webhook Name")
        .setAutoWidth(true).setFlexGrow(0).setResizable(true).setSortable(true);
    ;
    Grid.Column<Webhook> webhookUrlColumn = grid.addColumn(Webhook::getWebhookUrl).setHeader("Webhook URL")
        .setAutoWidth(true).setFlexGrow(0).setResizable(true).setSortable(true);
    ;

    // Create edit column & add edit button to it.
    Grid.Column<Webhook> editColumn = grid.addComponentColumn(webhook -> {
      Button editButton = new Button("Edit");
      // Edit button click listener.
      editButton.addClickListener(e -> {
        currentWebhook = webhook;
        if (editor.isOpen())
          editor.cancel();
        grid.getEditor().editItem(webhook);
      });
      editButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
      return editButton;
    }).setWidth("100px").setFlexGrow(0);

    // Create data binder for the Webhook class, and bind to the editor.
    Binder<Webhook> binder = new Binder<>(Webhook.class);
    editor.setBinder(binder);
    editor.setBuffered(true);

    TextField webhookUrlField = new TextField();
//    webhookUrlField.setWidthFull();
    binder.forField(webhookUrlField).bind(Webhook::getWebhookUrl, Webhook::setWebhookUrl);
    webhookUrlColumn.setEditorComponent(webhookUrlField);

    // Create buttons & add to a horizontal layout, then add that to the editor.
    Button saveButton = new Button("Save", e -> {
      try {
        editor.save();
        webhookService.update(currentWebhook);
        Notification.show("Webhook updated!", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      } catch (Exception exception) {
        Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    resetToDefaultsButton.addClickListener(e -> {
      try {
        resetToDefaults();
        Notification.show("Webhook URLs reset to their default values.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      } catch (Exception exception) {
        Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
    resetToDefaultsButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

    Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
    cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
    HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
    actions.setPadding(false);
    editColumn.setEditorComponent(actions);

    editor.addCancelListener(e -> {
      // Do nothing.
    });

//    grid.setAllRowsVisible(true);
    grid.setHeight("330px");
//    grid.setWidth("470px");
    grid.getStyle().set("resize", "horizontal");
    grid.getStyle().set("overflow", "auto");
    updateGrid();
    
    verticalLayout.add(titleWebhooks, grid, resetToDefaultsButton, new Hr());
    verticalLayout.add(titleWebhookDescriptions, webhookDescriptionsList);
    headerRow.add(verticalLayout);
    add(headerRow);
  }

  // Sorts in descending alphabetical order, A first, Z last.
  private class SortByName implements Comparator<Webhook> {
    // Used for sorting in descending order of ID
    public int compare(Webhook a, Webhook b) {
      return (int) (a.getWebhookName().compareTo(b.getWebhookName()));
    }
  }

  private void updateGrid() {
    // Load webhooks from the database & set grid items.
    List<Webhook> allWebhooks = webhookRepository.findAll();
    List<Webhook> webhooksToDisplay = new ArrayList<Webhook>();

    for (Webhook webhook : allWebhooks) {
      if (webhook.getWebhookName().equals("Developer Contact") || webhook.getWebhookName().equals("Logs (Debug)")
          || webhook.getWebhookName().equals("CE License Events")) {
        // Do nothing. These are irrelevant to admins so they don't need to be
        // displayed.
        // Developer Contact webhook is what the contact page depends on.
        // Debug Logs are obviously for the developer to debug things.
      } else {
        webhooksToDisplay.add(webhook);
      }
    }
    Collections.sort(webhooksToDisplay, new SortByName());
    grid.setItems(webhooksToDisplay);
  }

  public void resetToDefaults() {
    webhookRepository.deleteAll();

    // For contacting the developer on Discord via text-to-speech messages.
    Webhook contactWebhook = new Webhook();
    contactWebhook.setWebhookName("Developer Contact");
    contactWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/988724055765033000/tSmaOypQVKtCkDBzpWCLWIF-drMcLKun0Otjd0Rrt79evjno_4Bb9bxkYP86nK5F2-SP");
    webhookRepository.save(contactWebhook);

    // For notifying the developer of CollaborationEngine license events, as
    // documented at https://vaadin.com/docs/latest/tools/ce/going-to-production
    Webhook licenseEventsWebhook = new Webhook();
    licenseEventsWebhook.setWebhookName("CE License Events");
    licenseEventsWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/988366724682379294/g20NbSzfeL_QrZhZVWt-2rJh4I6MmSU_FtkPNv-9qeYq1MHbs5TKsv1g2NkMq8TLYT9o");
    webhookRepository.save(licenseEventsWebhook);

    // For backing up the audit logs to Discord.
    Webhook auditLogWebhook = new Webhook();
    auditLogWebhook.setWebhookName("Logs (Audit)");
    auditLogWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/988942093059784734/AUdjyyXznFlN1T7IovybkPlu6h_HEdVK4gE80uTgvRiIKEg7UXrvQaHvLtbV66zuwFRY");
    webhookRepository.save(auditLogWebhook);

    // For persisting chat logs to Discord (optional - if Admins desire this)
    Webhook chatLogsGeneralWebhook = new Webhook();
    chatLogsGeneralWebhook.setWebhookName("Logs (Chat #general)");
    chatLogsGeneralWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/990505609147338773/Mc95ah2V7y_FRxTpRbuafOyZ8cPFwYtsi5hjC0ZCJXJYcU8H_v_VMKImAxWyI39XCbxI");
    webhookRepository.save(chatLogsGeneralWebhook);

    // For persisting chat logs to Discord (optional - if Admins desire this)
    Webhook chatLogsNotesWebhook = new Webhook();
    chatLogsNotesWebhook.setWebhookName("Logs (Chat #notes)");
    chatLogsNotesWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/990507943042625576/jHMaf8WyhW7pt6--bJ1Svj1j60bFtQDKWxJKBJhc260OZg_pRjlHfMg5Gm6Ufvku9D0c");
    webhookRepository.save(chatLogsNotesWebhook);

    // For persisting chat logs to Discord (optional - if Admins desire this)
    Webhook chatLogsIssuesWebhook = new Webhook();
    chatLogsIssuesWebhook.setWebhookName("Logs (Chat #issues)");
    chatLogsIssuesWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/990508043160678460/RvEZrMfakiqBMRq1xRYEjyYCtR3-quSPSUGIWhIBF9meBgiw7o7K7TfbBn47vxN1ivHg");
    webhookRepository.save(chatLogsIssuesWebhook);

    // For logging the universally unique identifiers (UUIDS) of newly created
    // users. This allows us to re-use them to avoid surpassing the 20 user
    // CollaborationEngine monthly quota.
    Webhook userUuidWebhook = new Webhook();
    userUuidWebhook.setWebhookName("Logs (UUIDs)");
    userUuidWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/988570358691016784/MWE8EIOOh7-Eohofs0Dp6Wu6DiyEmr91hUcUXBMnyt6t0esLraN7XPTc-fKTNpfOjjvW");
    webhookRepository.save(userUuidWebhook);

    // For routing debug logs to Discord for developer usage.
    Webhook debugLogsWebhook = new Webhook();
    debugLogsWebhook.setWebhookName("Logs (Debug)");
    debugLogsWebhook.setWebhookUrl(
        "https://ptb.discord.com/api/webhooks/988568130093744218/xoLscoKMWCX3_7t63MESyA4FW3P_KSY6dlLB0hzYxbrqw6mTLlLsMXr7GlBbYd5rI3Ku");
    webhookRepository.save(debugLogsWebhook);

    updateGrid();
  }
}
