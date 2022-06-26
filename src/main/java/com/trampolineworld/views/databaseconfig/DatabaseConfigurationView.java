package com.trampolineworld.views.databaseconfig;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import org.json.JSONObject;

import com.trampolineworld.views.*;
import com.trampolineworld.views.schema.SchemaView;

@Route(value = "database", layout = MainLayout.class)
@PageTitle("Database Configuration")
@JavaScript("./js/script.js")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@RolesAllowed({ "ADMIN", "TECH" })
public class DatabaseConfigurationView extends HorizontalLayout {

  private Tabs tabs;
  private Tab tabTutorial, tabConnections;

  private Paragraph captionParagraph;
  private H2 title = new H2("What is this for?");

  private FormLayout layoutForm = new FormLayout();
  private TextField inputDataSourceUrl = new TextField();
  private TextField inputDataSourceUsername = new TextField();
  private PasswordField inputDataSourcePassword = new PasswordField();
  private Button buttonSave = new Button("Save Changes");
  private Button buttonReset = new Button("Reset to Defaults");

  private Paragraph captionList;
  private Button viewSchemaButton;
  private TextArea sqlGenerateSchema = new TextArea();

  VerticalLayout instructionsContainer = new VerticalLayout();

  HorizontalLayout buttonRow = new HorizontalLayout();
  VerticalLayout containerMain = new VerticalLayout();
  VerticalLayout container = new VerticalLayout();
  
  IFrame tutorialVideo = new IFrame();

  Button sqlCopyButton = new Button("Copy SQL Script");
  private String sqlStatement = ("CREATE SCHEMA trampolineworld;\r\n"
      + "\r\n"
      + "CREATE TABLE application_user ( \r\n"
      + "  id                   VARCHAR(200)  NOT NULL DEFAULT ('')   PRIMARY KEY,\r\n"
      + "  username             VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  display_name         VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  email                VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  hashed_password      VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  roles                SET('Value A','Value B')   DEFAULT (NULL)   ,\r\n"
      + "  profile_picture_url  VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  color_index          INT  NOT NULL DEFAULT ('0')   \r\n"
      + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\r\n"
      + "\r\n"
      + "CREATE TABLE audit_logs ( \r\n"
      + "  id                   VARCHAR(200)  NOT NULL    PRIMARY KEY,\r\n"
      + "  user_id              VARCHAR(200)  NOT NULL    ,\r\n"
      + "  username             VARCHAR(255)  NOT NULL    ,\r\n"
      + "  target_user_id       VARCHAR(200)   DEFAULT (NULL)   ,\r\n"
      + "  target_order_id      VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  customer_name        VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  action_category      VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  action_details       TEXT      ,\r\n"
      + "  timestamp            TIMESTAMP  NOT NULL DEFAULT (CURRENT_TIMESTAMP)   \r\n"
      + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\r\n"
      + "\r\n"
      + "CREATE TABLE chat_logs ( \r\n"
      + "  id                   VARCHAR(200)  NOT NULL    PRIMARY KEY,\r\n"
      + "  topic                VARCHAR(255)  NOT NULL    ,\r\n"
      + "  `text`               VARCHAR(255)  NOT NULL    ,\r\n"
      + "  author_id            VARCHAR(200)  NOT NULL    ,\r\n"
      + "  timestamp            TIMESTAMP  NOT NULL DEFAULT (CURRENT_TIMESTAMP)   \r\n"
      + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\r\n"
      + "\r\n"
      + "CREATE TABLE trampoline_order ( \r\n"
      + "  id                   BIGINT  NOT NULL  AUTO_INCREMENT  PRIMARY KEY,\r\n"
      + "  complete             TINYINT   DEFAULT (NULL)   ,\r\n"
      + "  first_name           VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  last_name            VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  phone_number         VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  email                VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  order_description    TEXT      ,\r\n"
      + "  measurements         VARCHAR(255)   DEFAULT (NULL)   ,\r\n"
      + "  subtotal             DOUBLE   DEFAULT (NULL)   ,\r\n"
      + "  total                DOUBLE   DEFAULT (NULL)   ,\r\n"
      + "  `date`               DATE   DEFAULT (NULL)   ,\r\n"
      + "  deleted              TINYINT   DEFAULT ('0')   \r\n"
      + " ) ENGINE=InnoDB AUTO_INCREMENT=70039 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\r\n"
      + "\r\n"
      + "CREATE TABLE user_roles ( \r\n"
      + "  user_id              VARCHAR(200)   DEFAULT (NULL)   ,\r\n"
      + "  roles                VARCHAR(255)   DEFAULT (NULL)   \r\n"
      + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\r\n"
      + "\r\n"
      + "CREATE TABLE webhooks ( \r\n"
      + "  id                   VARCHAR(200)  NOT NULL    PRIMARY KEY,\r\n"
      + "  webhook_name         VARCHAR(255)  NOT NULL    ,\r\n"
      + "  webhook_url          VARCHAR(255)  NOT NULL    \r\n"
      + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;\r\n"
      + "");

  public DatabaseConfigurationView() {
    // Configure parent layout.
    addClassNames("userguide-view");
    setId("userguide-view");
    setClassName("userguide-layout");
    setWidthFull();
    getStyle().set("opacity", "0.8 !important");
//    setSizeFull();
    container.setSizeFull();

    configureTabs();
    configureTutorialInstructions();
    configureConnectionsForm();
    configureTutorialVideo();

    loadTutorialTab();

    containerMain.add(tabs, container);
    add(containerMain);

    // Listen for tab changes, load layout components accordingly.
    tabs.addSelectedChangeListener(event -> {

      Optional<String> tabID = event.getSelectedTab().getId();

      if (tabID.equals(tabTutorial.getId())) {
        loadTutorialTab();
      } else if (tabID.equals(tabConnections.getId())) {
        loadConnectionsTab();
      }
    });
  }

  private void configureTutorialInstructions() {
    title.getStyle().set("margin-top", "0px !important");
    captionParagraph = new Paragraph(
        "In the event that you ever wish to use your own database for storing this system's data, you'll first have to create one with an identical structure to the one that this system currently relies on, and then you'll have to connect this application to it."
            + "\n\nIt's critical that the database schema (design/structure) is the same as the current one. This means you'll need to re-create the same tables with the exact same columns, names, data-types, constraints, and primary keys."
            + "\n\nWhile that may sound complicated, it's as simple as running a simple script that I've provided for you. You'll find that script at the bottom of this page, alongside a video tutorial that will walk you through this entire process, beginning to end. From generating the database, schema, and tables, to connecting them to this application.");

    viewSchemaButton = new Button("Schema / Database Structure");
    viewSchemaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    viewSchemaButton.addClickListener(e -> {
      UI.getCurrent().navigate(SchemaView.class);
    });

    captionList = new Paragraph(
        "In shorter summary, there are only two steps to configuring this application to use a database of your choosing.");

    OrderedList listInstructions = new OrderedList();
    ListItem step1 = new ListItem("Create the database with a matching schema.");
    ListItem step2 = new ListItem("Change the values of this application's connection variables to point to your new database.");
    listInstructions.add(step1, step2);
    listInstructions.getStyle().set("margin-top", "0px");
    listInstructions.getStyle().set("margin-bottom", "0px");

    Paragraph videoCaption = new Paragraph(
        "The below video tutorial is provided to more thoroughly guide you through this process.");
    // Video here.

    Paragraph sqlInstructions = new Paragraph(
        "While it's entirely possible to recreate the schema by hand in a visual database editor (such as DbSchema or phpMyAdmin), the easiest way to create the schema is by executing a simple SQL script. The script below, if executed in a MySQL database, will automatically create the schema you need as well as all six tables that this system runs on.");

    sqlGenerateSchema.setValue(sqlStatement);
    sqlGenerateSchema.setWidthFull();
    sqlGenerateSchema.setReadOnly(true);

    
    sqlCopyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    sqlCopyButton.addClickListener(e -> {
      UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", sqlGenerateSchema.getValue());
      Notification.show("Copied to clipboard!", 4000, Position.TOP_CENTER)
      .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    instructionsContainer.add(title, captionParagraph, viewSchemaButton, captionList, listInstructions, videoCaption, tutorialVideo,
        new Hr(), sqlInstructions, sqlCopyButton, sqlGenerateSchema);
  }

  private void loadTutorialTab() {
    container.removeAll();
    container.add(instructionsContainer);
  }

  private void configureConnectionsForm() {
    // Configure buttons.
    buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
    buttonReset.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
    buttonSave.setWidth("50%");
    buttonReset.setWidth("50%");

    // False means don't reset variables & use ones from the input fields, true
    // means reset to defaults.
    buttonSave.addClickListener(e -> updateEnvironmentVariables(false));
    buttonReset.addClickListener(e -> updateEnvironmentVariables(true));

    // Configure button row.
    buttonRow.add(buttonSave, buttonReset);
    buttonRow.setSpacing(true);
    buttonRow.setAlignItems(Alignment.BASELINE);

    // Add input fields to the form.
    layoutForm.addFormItem(inputDataSourceUrl, "Database URL");
    layoutForm.addFormItem(inputDataSourceUsername, "Database Username");
    layoutForm.addFormItem(inputDataSourcePassword, "Database Password");
    layoutForm.add(new Hr(), buttonRow);
    layoutForm.setWidth("300px");
    layoutForm.setResponsiveSteps(
        // Use one column by default
        new ResponsiveStep("0", 1));
  }

  private void loadConnectionsTab() {
    container.removeAll();
    container.add(layoutForm);
  }

  private void configureTabs() {
    tabTutorial = new Tab("Tutorial");
    tabTutorial.setId("tabTutorial");

    tabConnections = new Tab("Connection Variables");
    tabConnections.setId("tabDbConfig");

    tabs = new Tabs(tabTutorial, tabConnections);
    tabs.setWidthFull();
  }
  
  private void configureTutorialVideo() {
    tutorialVideo.getElement().setAttribute("src", "https://www.youtube.com/embed/t65jdhNLmm8");
    tutorialVideo.getElement().setAttribute("title", "YouTube video player");
    tutorialVideo.getElement().setAttribute("frameborder", "0");
    tutorialVideo.getElement().setAttribute("allow", "accelerometer");
    tutorialVideo.getElement().setAttribute("autoplay", "true");
    tutorialVideo.getElement().setAttribute("allowfullscreen", "true");    
  }

  private void updateEnvironmentVariables(boolean reset) {

    try {
      URL url = new URL("https://api.heroku.com/apps/trampolineworld/config-vars");
      HttpURLConnection http = (HttpURLConnection) url.openConnection();

      // Patch fix.
      http.setRequestProperty("X-HTTP-Method-Override", "PATCH");
      http.setRequestMethod("POST");

      http.setDoOutput(true);
      http.setRequestProperty("Content-Type", "application/json");
      http.setRequestProperty("Accept", "application/vnd.heroku+json; version=3");
      http.setRequestProperty("Authorization", "Bearer cdc36c38-a8e0-4e3d-a904-17d58b1b49e2");

      JSONObject configVars = new JSONObject();
      String stringConfigVars;

      if (reset) {
        configVars.put("spring.datasource.url", "jdbc:mysql://162.241.244.22/faintdev_trampolineworld");
        configVars.put("spring.datasource.username", "faintdev_twadmin");
        configVars.put("spring.datasource.password", "beautifulstranger410");
        stringConfigVars = configVars.toString();
      } else {
        configVars.put("spring.datasource.url", inputDataSourceUrl.getValue());
        configVars.put("spring.datasource.username", inputDataSourceUsername.getValue());
        configVars.put("spring.datasource.password", inputDataSourcePassword.getValue());
        stringConfigVars = configVars.toString();
      }

      byte[] out = stringConfigVars.getBytes(StandardCharsets.UTF_8);

      OutputStream stream = http.getOutputStream();
      stream.write(out);

      System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
      http.disconnect();
      Notification.show("Environment variables updated!", 4000, Position.TOP_CENTER)
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    } catch (Exception exception) {
      Notification.show(exception.toString(), 4000, Position.TOP_CENTER)
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
  }
}