package com.trampolineworld.views.auditlog;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.LogEntry;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.LogEntryService;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.utilities.DiscordWebhook;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Audit Log")
@Route(value = "auditlog", layout = MainLayout.class)
@RolesAllowed({ "ADMIN" })
@Uses(Icon.class)
@CssImport(themeFor = "vaadin-grid", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(value = "./themes/trampolineworld/views/dialog.css", themeFor = "vaadin-dialog-overlay")
public class AuditLogView extends Div implements BeforeEnterObserver {

  private Grid<LogEntry> grid = new Grid<>(LogEntry.class, false);
  H2 editTitle;
  private TextField filterTextField = new TextField();
  private HorizontalLayout buttonHeaderContainer = new HorizontalLayout();
  private LogEntryService logEntryService;

  private Grid.Column<LogEntry> columnUserId, columnUsername, columnActionCategory, 
  columnActionDetails, columnCustomerName, columnTargetOrderId, columnTargetUserId, columnTimestamp;

  @Autowired
  public AuditLogView(LogEntryService logEntryService) {
    addClassNames("trampoline-orders-view");
    this.logEntryService = logEntryService;

    // Configure the grid.
    configureGrid(logEntryService);

    // Create button header bar.
    createButtonHeader(); // Requires splitLayout argument to define button functions.
    
    // Add buttonHeaderContainer and splitLayout to view.
    add(buttonHeaderContainer);
    
    add(grid);
  }

  private void configureGrid(LogEntryService logEntryService) {
    grid.setColumnReorderingAllowed(true);
    grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
//    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    // Add columns to the grid.
    columnUserId =  grid.addColumn("userId").setAutoWidth(true).setResizable(true).setHeader("User ID");
    columnUsername = grid.addColumn("username").setAutoWidth(true).setResizable(true);
    columnActionCategory = grid.addColumn(createStatusComponentRenderer()).setHeader("Action Category").setAutoWidth(true).setResizable(true);
    columnActionDetails = grid.addColumn("actionDetails").setAutoWidth(true).setResizable(true);
    columnCustomerName = grid.addColumn("customerName").setAutoWidth(true).setResizable(true);
    columnTargetOrderId = grid.addColumn("targetOrderId").setAutoWidth(true).setResizable(true);
    columnTargetUserId = grid.addColumn("targetUserId").setAutoWidth(true).setResizable(true);
    columnTimestamp = grid.addColumn("timestamp").setAutoWidth(true).setResizable(true);
    
    // Make certain columns invisible by default.
    columnUserId.setVisible(false);
    columnUsername.setVisible(false);
    columnCustomerName.setVisible(false);
    columnTargetOrderId.setVisible(false);
    columnTargetUserId.setVisible(false);
    
    updateGrid();
  }

  private void updateGrid() {
    List<LogEntry> allLogs = logEntryService.findAll(filterTextField.getValue());
    List<LogEntry> formattedLogs = new ArrayList<LogEntry>();
    
    for (LogEntry log : allLogs) {
      String stringTimestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(log.getTimestamp());
      Timestamp formattedTimestamp = Timestamp.valueOf(stringTimestamp);
      log.setTimestamp(formattedTimestamp);
      formattedLogs.add(log);
    }
    grid.setItems(formattedLogs);
//    grid.setItems(logEntryService.findAll(filterTextField.getValue()));
//    grid.setItems(logEntryService.findAllNoFilter());
  }

  // Logic for generating role badges.
  private static final SerializableBiConsumer<Span, LogEntry> statusComponentUpdater = (span, logEntry) -> {

    String actionCategory = logEntry.getActionCategory();
    
    Span badge = new Span(actionCategory);
    badge.getElement().getStyle().set("margin", "3px");
    
    
    if (actionCategory.equals("Created Order")) {
      badge.getElement().getThemeList().add("badge success pill");
    } else if (actionCategory.equals("Edited Order")) {
      badge.getElement().getThemeList().add("badge pill");
    } else if (actionCategory.equals("Deleted Order")) {
      badge.getElement().getThemeList().add("badge error pill");
    } else if (actionCategory.equals("Created User")) {
      badge.getElement().getThemeList().add("badge success pill");
    } else if (actionCategory.equals("Edited User")) {
      badge.getElement().getThemeList().add("badge pill");
    } else if (actionCategory.equals("Restored Order") || actionCategory.equals("Restored Orders")) {
      badge.getElement().getThemeList().add("badge success pill");
    }
    

    span.add(badge);
  };

  // For rendering a more complex component in a grid cell.
  private static ComponentRenderer<Span, LogEntry> createStatusComponentRenderer() {
    return new ComponentRenderer<>(Span::new, statusComponentUpdater);
  }

  private void createButtonHeader() {
    // Configure button header container.
    buttonHeaderContainer.setSpacing(false);
    buttonHeaderContainer.setAlignItems(Alignment.BASELINE);

    filterTextField.setPlaceholder("Search...");
    filterTextField.setHelperText("Filter name, action, or id");
    filterTextField.setClearButtonVisible(true);
    filterTextField.setValueChangeMode(ValueChangeMode.LAZY); // Don't hit database on every keystroke. Wait for
                                  // user to finish typing.
    filterTextField.addValueChangeListener(e -> updateGrid());
    
        Button menuButton = new Button("Show/Hide Columns");
        menuButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        menuButton.getStyle().set("margin-right", "6px");
        menuButton.getElement().setAttribute("title", "There are additional column options, click me to reveal them!");
        
        ColumnToggleContextMenu columnToggleContextMenu = new ColumnToggleContextMenu(
                menuButton);
        columnToggleContextMenu.addColumnToggleItem("User ID", columnUserId);
        columnToggleContextMenu.addColumnToggleItem("Username", columnUsername);
        columnToggleContextMenu.addColumnToggleItem("Action Category", columnActionCategory);
        columnToggleContextMenu.addColumnToggleItem("Action Details", columnActionDetails);
        columnToggleContextMenu.addColumnToggleItem("Customer Name", columnCustomerName);
        columnToggleContextMenu.addColumnToggleItem("Target Order ID", columnTargetOrderId);
        columnToggleContextMenu.addColumnToggleItem("Target User ID", columnTargetUserId);
        columnToggleContextMenu.addColumnToggleItem("Timestamp", columnTimestamp);

    buttonHeaderContainer.add(menuButton, filterTextField);
  }
  
    private static class ColumnToggleContextMenu extends ContextMenu {
        public ColumnToggleContextMenu(Component target) {
            super(target);
            setOpenOnClick(true);
        }

        void addColumnToggleItem(String label, Grid.Column<LogEntry> column) {
            MenuItem menuItem = this.addItem(label, e -> {
                column.setVisible(e.getSource().isChecked());
            });
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
        }
    }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    
  }
}