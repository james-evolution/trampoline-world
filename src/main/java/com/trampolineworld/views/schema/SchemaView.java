package com.trampolineworld.views.schema;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
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

@Route(value = "schema", layout = MainLayout.class)
@PageTitle("Database Schema")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@RolesAllowed({ "ADMIN", "TECH" })
public class SchemaView extends HorizontalLayout {

  private Tabs tabs;
  private Tab tabSchema, tabTutorial, tabDbConfig;

  private IFrame schemaFrame;
  private Paragraph captionParagraph;
  private H2 title = new H2("What is this for?");

  private FormLayout layoutForm = new FormLayout();
  private TextField inputDataSourceUrl = new TextField();
  private TextField inputDataSourceUsername = new TextField();
  private PasswordField inputDataSourcePassword = new PasswordField();
  private Button buttonSave = new Button("Save Changes");
  private Button buttonReset = new Button("Reset to Defaults");

  VerticalLayout instructionsContainer = new VerticalLayout();

  HorizontalLayout buttonRow = new HorizontalLayout();
  VerticalLayout containerMain = new VerticalLayout();
  VerticalLayout container = new VerticalLayout();

  public SchemaView() {
    // Configure parent layout.
    addClassNames("userguide-view");
    setId("userguide-view");
    setClassName("userguide-layout");
    setWidthFull();
    getStyle().set("opacity", "0.8 !important");
    setSizeFull();
    container.setSizeFull();

    Button goBackButton = new Button("Go Back");
    // Configure button appearance and click listener.
    goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    goBackButton.addClickListener(e -> {
      History history = UI.getCurrent().getPage().getHistory();
      history.back();
    });
    
    configureSchemaFrame();
    container.add(schemaFrame);
    containerMain.add(goBackButton, container);
    add(containerMain);

  }

  private void configureSchemaFrame() {
    schemaFrame = new IFrame();
    schemaFrame.setSrc("https://faintdev.net/trampolineworld/schema3.html");
    schemaFrame.getStyle().set("width", "100% !important");
    schemaFrame.getStyle().set("height", "100% !important");
    schemaFrame.getStyle().set("box-shadow", "none !important");
    schemaFrame.getStyle().set("border-style", "none !important");
  }
}