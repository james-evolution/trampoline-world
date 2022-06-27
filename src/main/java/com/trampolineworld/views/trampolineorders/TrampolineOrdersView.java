package com.trampolineworld.views.trampolineorders;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.LogEntry;
import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.LogEntryRepository;
import com.trampolineworld.data.service.LogEntryService;
import com.trampolineworld.data.service.TrampolineOrderRepository;
import com.trampolineworld.data.service.TrampolineOrderService;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.data.service.WebhookRepository;
import com.trampolineworld.security.AuthenticatedUser;
import com.trampolineworld.views.MainLayout;
import com.trampolineworld.views.viewsingleorder.ViewSingleOrder;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("All Orders")
@Route(value = "trampoline_orders/:trampolineOrderID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "TECH", "USER"})
@Uses(Icon.class)
@CssImport(themeFor = "vaadin-grid", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(themeFor = "vaadin-dialog-overlay", value = "./themes/trampolineworld/views/dialog.css")

public class TrampolineOrdersView extends Div implements BeforeEnterObserver {

  private User currentUser;
  private String currentActionCategory;
  private String currentActionDetails;

  public static String username = "";

  private final String TRAMPOLINEORDER_ID = "trampolineOrderID";
  private final String TRAMPOLINEORDER_EDIT_ROUTE_TEMPLATE = "trampoline_orders/%s/edit";
  private final String TRAMPOLINEORDER_VIEW_ROUTE_TEMPLATE = "view_order/%s";
  private Long targetId;
  private Grid<TrampolineOrder> grid = new Grid<>(TrampolineOrder.class, false);
  CollaborationAvatarGroup avatarGroup;
  H2 editTitle;
  
  private TextField inputSearchFilter = new TextField();
  private Checkbox inputComplete;
  private TextField inputFirstName, inputLastName, inputPhoneNumber, inputEmail, inputSubtotal, inputTotal;
  private TextArea inputOrderDescription, inputMeasurements;
  private DatePicker inputDate;

  private GridContextMenu<TrampolineOrder> menu;
  private Div editorLayoutDiv;
  private HorizontalLayout buttonHeaderContainer = new HorizontalLayout();

  private Dialog confirmDeleteDialog = new Dialog();

  private Button buttonCancel = new Button("Cancel");
  private Button buttonSave = new Button("Save");
  private Button buttonNewOrder = new Button("New Order");
  private Button buttonHideSidebar = new Button("Cancel");

  private CollaborationBinder<TrampolineOrder> binder;
  private TrampolineOrder trampolineOrder;
  private final TrampolineOrderService trampolineOrderService;
  private final TrampolineOrderRepository trampolineOrderRepository;
  private final UserService userService;
  private final UserRepository userRepository;
  private final LogEntryRepository logEntryRepository;
  private final WebhookRepository webhookRepository;
  
  private Grid.Column<TrampolineOrder> columnId, columnComplete, columnFirstName, columnLastName,
  columnPhoneNumber, columnEmail, columnOrderDescription, columnMeasurements, columnSubtotal,
  columnTotal, columnDate;

  @Autowired
  public TrampolineOrdersView(TrampolineOrderService trampolineOrderService,
      TrampolineOrderRepository trampolineOrderRepository, UserService userService, UserRepository userRepository,
      LogEntryRepository logEntryRepository, WebhookRepository webhookRepository) {
    this.trampolineOrderService = trampolineOrderService;
    this.trampolineOrderRepository = trampolineOrderRepository;
    this.userService = userService;
    this.userRepository = userRepository;
    this.logEntryRepository = logEntryRepository;
    this.webhookRepository = webhookRepository;
    addClassNames("trampoline-orders-view");

//      Notification.show("Welcome, " + username, 4000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
    currentUser = userRepository.findByUsername(currentUsername);

    // UserInfo is used by Collaboration Engine and is used to share details
    // of users to each other to able collaboration. Replace this with
    // information about the actual user that is logged, providing a user
    // identifier, and the user's real name. You can also provide the users
    // avatar by passing an url to the image as a third parameter, or by
    // configuring an `ImageProvider` to `avatarGroup`.
    UserInfo userInfo = new UserInfo(currentUser.getId().toString(), currentUser.getDisplayName());
    userInfo.setImage(currentUser.getProfilePictureUrl());
    userInfo.setColorIndex(currentUser.getColorIndex());

    // Create split-view UI
    SplitLayout splitLayout = new SplitLayout();
    splitLayout.getStyle().set("margin-top", "0px !important");    
    splitLayout.getStyle().set("border-style", "solid");
    splitLayout.getStyle().set("border-top", "1px");
    splitLayout.getStyle().set("border-bottom", "0px");
    splitLayout.getStyle().set("border-left", "0px");
    splitLayout.getStyle().set("border-right", "0px");
    splitLayout.getStyle().set("border-color", "rgb(5, 57, 54)");
    
    // Configure avatar group (users / pfps)
    avatarGroup = new CollaborationAvatarGroup(userInfo, null);
    avatarGroup.getStyle().set("visibility", "visible");

    // Create grid and editor layouts.
    createGridLayout(splitLayout);
    createEditorLayout(splitLayout);

    // Configure the grid.
    configureGrid(splitLayout);

    // Create button header bar.
    createButtonHeader(splitLayout); // Requires splitLayout argument to define button functions.

    // Add buttonHeaderContainer and splitLayout to view.
    add(buttonHeaderContainer);
    add(splitLayout);

    // Create context menu.
    createContextMenu(); // View & Delete buttons.


    // Configure & add delete confirmation dialog.
    configureDeleteDialog();
    add(confirmDeleteDialog);

    // Configure the form.
    configureForm(userInfo);
    configureFormButtons();
  }

  private void configureDeleteDialog() {
    confirmDeleteDialog.setHeaderTitle("Delete Order");
    confirmDeleteDialog.setDraggable(true);
    confirmDeleteDialog.addClassName("deleteDialog");

    VerticalLayout dialogLayout = createDialogLayout();
    confirmDeleteDialog.add(dialogLayout);

    Button dialogDeleteOrderButton = createDialogDeleteOrderButton(confirmDeleteDialog, trampolineOrderService);
    Button cancelButton = new Button("Cancel", e -> confirmDeleteDialog.close());
    confirmDeleteDialog.getFooter().add(dialogDeleteOrderButton);
    confirmDeleteDialog.getFooter().add(cancelButton);
  }

  private void configureFormButtons() {
    // When the cancel button is clicked, clear the form and refresh the grid.
    buttonCancel.addClickListener(e -> {
      clearForm();
//            refreshGrid();
      updateGrid();
      editorLayoutDiv.setVisible(false);
      buttonHideSidebar.setVisible(false);
    });

    // When the save button is clicked, save the new order.
    buttonSave.addClickListener(e -> {
      try {
        if (this.trampolineOrder == null) {
          this.trampolineOrder = new TrampolineOrder();
        }
        binder.writeBean(this.trampolineOrder);
        trampolineOrderService.update(this.trampolineOrder);

        String customerName = this.trampolineOrder.getFirstName() + " " + this.trampolineOrder.getLastName();
        
        // Log order edited action.
        if (currentActionCategory == "Edited Order") {
          LogEntry logEntry = new LogEntry(
              logEntryRepository,
              webhookRepository,
              currentUser.getId(), 
              currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")",
              this.trampolineOrder.getId().toString(), 
              customerName, 
              currentActionCategory,
              currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")" + currentActionDetails + this.trampolineOrder.getId().toString() + " for " + customerName
            );
        } else if (currentActionCategory == "Created Order") {
//          // Get all trampoline orders from the database.
//          List<TrampolineOrder> allOrders = trampolineOrderRepository.findAll();
//          // Sort by id.
//          Collections.sort(allOrders, new SortById());
//          // This is one method of getting the last order's id.
//          allOrders.get(0).getId();
          // Log new order created action.
          LogEntry logEntry = new LogEntry(
              logEntryRepository, 
              webhookRepository,
              currentUser.getId(), 
              currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")",
              this.trampolineOrder.getId().toString(), 
              customerName, 
              currentActionCategory,
              currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")" + currentActionDetails + this.trampolineOrder.getId().toString() + " for " + customerName
            );
        }

        clearForm();
        updateGrid();
        editorLayoutDiv.setVisible(false);
        buttonHideSidebar.setVisible(false);
        Notification.show("Order details stored.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate(TrampolineOrdersView.class);
      } catch (ValidationException validationException) {
        Notification.show("Invalid form input.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
  }

  private class SortById implements Comparator<TrampolineOrder> {
    // Used for sorting in descending order of ID
    public int compare(TrampolineOrder a, TrampolineOrder b) {
      return (int) (b.getId() - a.getId());
    }
  }

  private void configureForm(UserInfo userInfo) {
    binder = new CollaborationBinder<>(TrampolineOrder.class, userInfo);
    // Bind fields. This is where you'd define e.g. validation rules
    binder.forField(inputFirstName, String.class).bind("firstName");
    binder.forField(inputLastName, String.class).bind("lastName");
    binder.forField(inputPhoneNumber, String.class).bind("phoneNumber");
    binder.forField(inputEmail, String.class).bind("email");
    binder.forField(inputOrderDescription, String.class).bind("orderDescription");
    binder.forField(inputMeasurements, String.class).bind("measurements");
    binder.forField(inputComplete, Boolean.class).bind("complete");
    binder.forField(inputSubtotal, String.class).asRequired("Subtotal field cannot be empty.")
        .withConverter(new StringToDoubleConverter("Only numbers are allowed")).bind("subtotal");
    binder.forField(inputTotal, String.class).asRequired("Total field cannot be empty.")
        .withConverter(new StringToDoubleConverter("Only numbers are allowed")).bind("total");
    binder.forField(inputDate, LocalDate.class).asRequired("Date field cannot be empty.").bind("date");
    binder.bindInstanceFields(this);
  }
  
  private static Component createHeaderTotal() {
    Span span = new Span("Total");
    Icon icon = VaadinIcon.DOLLAR.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }
  private static Component createHeaderSubtotal() {
    Span span = new Span("Subtotal");
    Icon icon = VaadinIcon.DOLLAR.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }
  private static Component createHeaderDescription() {
    Span span = new Span("Order Description");
    Icon icon = VaadinIcon.INFO_CIRCLE_O.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }
  private static Component createHeaderMeasurements() {
    Span span = new Span("Measurements");
    Icon icon = VaadinIcon.SCISSORS.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }
  private static Component createHeaderPhoneNumber() {
    Span span = new Span("Phone Number");
    Icon icon = new Icon("lumo", "phone");
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }
  private static Component createHeaderEmail() {
    Span span = new Span("Email");
    Icon icon = VaadinIcon.ENVELOPE.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }
  private static Component createHeaderDate() {
    Span span = new Span("Date");
    Icon icon = VaadinIcon.CALENDAR.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)")
    .set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }

  private void configureGrid(SplitLayout splitLayout) {
    grid.setColumnReorderingAllowed(true);
    grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
    grid.addThemeVariants(GridVariant.LUMO_COMPACT);
//    grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

    // Add columns to the grid.
//    grid.addColumn(createStatusComponentRenderer()).setAutoWidth(true).setResizable(true);
    columnId = grid.addColumn("id").setAutoWidth(true).setResizable(true);
    columnComplete = grid.addColumn("complete").setAutoWidth(true).setResizable(true);
    columnFirstName = grid.addColumn("firstName").setAutoWidth(true).setResizable(true);
    columnLastName = grid.addColumn("lastName").setAutoWidth(true).setResizable(true);
    columnPhoneNumber = grid.addColumn("phoneNumber").setAutoWidth(true).setResizable(true).setHeader(createHeaderPhoneNumber());
    columnEmail = grid.addColumn("email").setAutoWidth(true).setResizable(true).setHeader(createHeaderEmail());
    columnOrderDescription = grid.addColumn("orderDescription").setWidth("300px").setResizable(true).setHeader(createHeaderDescription());
    columnMeasurements = grid.addColumn("measurements").setWidth("300px").setResizable(true).setHeader(createHeaderMeasurements());
    columnSubtotal = grid.addColumn("subtotal").setAutoWidth(true).setResizable(true).setHeader(createHeaderSubtotal());
    columnTotal = grid.addColumn("total").setAutoWidth(true).setResizable(true).setHeader(createHeaderTotal());
    columnDate = grid.addColumn("date").setAutoWidth(true).setResizable(true).setHeader(createHeaderDate());
    
    updateGrid();

//        grid.setItems(query -> trampolineOrderService.list(
//                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                .stream());

    // Class name generator.
    grid.setClassNameGenerator(order -> {
      if (order.isComplete()) {
        return "complete";
      } else {
        return "incomplete";
      }
    });

    // When a row is selected or deselected, populate form.
    grid.asSingleSelect().addValueChangeListener(event -> {
      if (event.getValue() != null) {
        editorLayoutDiv.setVisible(true);
        buttonHideSidebar.setVisible(true);
        splitLayout.setSplitterPosition(0);
        UI.getCurrent().navigate(String.format(TRAMPOLINEORDER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
      } else {
        editorLayoutDiv.setVisible(false);
        clearForm();
        UI.getCurrent().navigate(TrampolineOrdersView.class);
      }
    });
  }

  private void updateGrid() {
    grid.setItems(trampolineOrderService.findAll(inputSearchFilter.getValue()));
  }

  private static final SerializableBiConsumer<Span, TrampolineOrder> statusComponentUpdater = (span, order) -> {
    boolean complete = order.isComplete();
    String theme = String.format("badge %s", complete ? "success" : "error");
    span.getElement().setAttribute("theme", theme);

    if (order.isComplete()) {
      span.setText("Complete");
    } else {
      span.setText("Incomplete");
    }
  };

  private static ComponentRenderer<Span, TrampolineOrder> createStatusComponentRenderer() {
    return new ComponentRenderer<>(Span::new, statusComponentUpdater);
  }

  private void createContextMenu() {
    // Add the context menu to the grid.
    menu = grid.addContextMenu();

    // Listen for the event in which the context menu is opened, then save the id of
    // the TrampolineOrder that was right clicked on.
    menu.addGridContextMenuOpenedListener(event -> {
      targetId = event.getItem().get().getId();
      Set<Role> roles = currentUser.getRoles();
      // If user is not an admin or a tech, hide the 'delete' option.
      if (!(roles.contains(Role.ADMIN) || roles.contains(Role.TECH))) {
        List<GridMenuItem<TrampolineOrder>> menuItems = menu.getItems();
        menuItems.get(1).setVisible(false);
      }
    });
    // Add menu items to the grid, send user to the 'ViewSingleOrder' page with the
    // TrampolineOrder id as an argument.
    menu.addItem("View", event -> {
      UI.getCurrent().navigate(String.format(TRAMPOLINEORDER_VIEW_ROUTE_TEMPLATE, targetId.toString()));
    });
    menu.addItem("Delete", event -> {
      try {
        confirmDeleteDialog.open();
      } catch (Exception e) {
        Notification.show("An error has occurred, please contact the developer.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
  }

  private void createButtonHeader(SplitLayout splitLayout) {
    // Configure button header container.
    buttonHeaderContainer.setSpacing(false);
    buttonHeaderContainer.setAlignItems(Alignment.BASELINE);

    buttonNewOrder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    buttonNewOrder.getElement().getStyle().set("margin-left", "6px");
    buttonNewOrder.getElement().getStyle().set("margin-right", "6px");
    buttonNewOrder.addClickListener(e -> {
      clearForm();
      updateGrid();
      editorLayoutDiv.setVisible(true);
      buttonHideSidebar.setVisible(true);
      splitLayout.setSplitterPosition(0);
    });

//    hideSidebarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    buttonHideSidebar.getElement().getStyle().set("margin-left", "0px !important");
    buttonHideSidebar.getElement().getStyle().set("margin-right", "6px");
    buttonHideSidebar.setVisible(false);

    buttonHideSidebar.addClickListener(e -> {
      clearForm();
      updateGrid();
      editorLayoutDiv.setVisible(false);
      buttonHideSidebar.setVisible(false);
    });

    inputSearchFilter.setPlaceholder("Search...");
    inputSearchFilter.setHelperText("Filter by name, email, or number");
    inputSearchFilter.setClearButtonVisible(true);
    inputSearchFilter.setValueChangeMode(ValueChangeMode.LAZY); // Don't hit database on every keystroke. Wait for
    inputSearchFilter.addValueChangeListener(e -> updateGrid());

        Button menuButton = new Button("Show/Hide Columns");
        menuButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        menuButton.getStyle().set("margin-right", "6px");
        menuButton.getElement().setAttribute("title", "There are additional column options, click me to reveal them!");
        
        ColumnToggleContextMenu columnToggleContextMenu = new ColumnToggleContextMenu(
                menuButton);
        columnToggleContextMenu.addColumnToggleItem("ID", columnId);
        columnToggleContextMenu.addColumnToggleItem("Complete", columnComplete);
        columnToggleContextMenu.addColumnToggleItem("First Name", columnFirstName);
        columnToggleContextMenu.addColumnToggleItem("Last Name", columnLastName);
        columnToggleContextMenu.addColumnToggleItem("Phone Number", columnPhoneNumber);
        columnToggleContextMenu.addColumnToggleItem("Email", columnEmail);
        columnToggleContextMenu.addColumnToggleItem("Order Description", columnOrderDescription);
        columnToggleContextMenu.addColumnToggleItem("Measurements", columnMeasurements);
        columnToggleContextMenu.addColumnToggleItem("Subtotal", columnSubtotal);
        columnToggleContextMenu.addColumnToggleItem("Total", columnTotal);
        columnToggleContextMenu.addColumnToggleItem("Date", columnDate);  

//    buttonHeaderContainer.add(menuButton, filterTextField, newOrderButton, hideSidebarButton);
    buttonHeaderContainer.getStyle().set("margin-bottom", "0px !important");
        buttonHeaderContainer.add(menuButton, inputSearchFilter, buttonNewOrder);
  }
  
    private static class ColumnToggleContextMenu extends ContextMenu {
        public ColumnToggleContextMenu(Component target) {
            super(target);
            setOpenOnClick(true);
        }

        void addColumnToggleItem(String label, Grid.Column<TrampolineOrder> column) {
            MenuItem menuItem = this.addItem(label, e -> {
                column.setVisible(e.getSource().isChecked());
            });
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
        }
    }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Optional<Long> trampolineOrderId = event.getRouteParameters().get(TRAMPOLINEORDER_ID).map(Long::valueOf);
    if (trampolineOrderId.isPresent()) {
      Optional<TrampolineOrder> trampolineOrderFromBackend = trampolineOrderService.get(trampolineOrderId.get());
      if (trampolineOrderFromBackend.isPresent()) {
        populateForm(trampolineOrderFromBackend.get());
      } else {
        Notification
            .show(String.format("The requested trampolineOrder was not found, ID = %d",
                trampolineOrderId.get()), 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        // When a row is selected but the data is no longer available, update the grid
        // component.
        updateGrid();
        event.forwardTo(TrampolineOrdersView.class);
      }
    }
  }

  private static VerticalLayout createDialogLayout() {

    Paragraph confirmationMessage = new Paragraph("Are you sure you want to delete this order?");
    VerticalLayout dialogLayout = new VerticalLayout(confirmationMessage);
    dialogLayout.setPadding(false);
    dialogLayout.setSpacing(false);
//        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
    dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

    return dialogLayout;
  }

  private Button createDialogDeleteOrderButton(Dialog dialog, TrampolineOrderService trampolineOrderService) {
    Button dialogDeleteButton = new Button("Delete", e -> {
      try {
        Optional<TrampolineOrder> orderToDelete = trampolineOrderRepository.findById(targetId);
        
        // Get customer name.
        String customerName = orderToDelete.get().getFirstName() + " " + orderToDelete.get().getLastName();
        // Delete order from database.
        /*
         * UPDATE: Instead of deleting from the database, we'll mark the order as deleted.
         * Orders marked as deleted will not show up in the grid.
         * However, they will remain archived in the database for record-keeping.
         */
        trampolineOrderService.delete(targetId);
        
        // Log action.
        currentActionCategory = "Deleted Order";
        currentActionDetails = " deleted order #" + targetId.toString() + " for " + customerName;
        LogEntry logEntry = new LogEntry(
            logEntryRepository,
            webhookRepository,
            currentUser.getId(), 
            currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")",
            targetId.toString(), 
            customerName, 
            currentActionCategory,
            currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")" + currentActionDetails);        
        // Notify of deletion success.
        Notification.show("Order deleted.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        // Notify of deletion failure.
      } catch (Exception exception) {
        Notification.show("Operation failed.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
      dialog.close();
      updateGrid();
    });
    dialogDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

    return dialogDeleteButton;
  }

  private void createEditorLayout(SplitLayout splitLayout) {
    editTitle = new H2("New Order");
    editorLayoutDiv = new Div();
    editorLayoutDiv.setClassName("editor-layout");
    editorLayoutDiv.setVisible(false);

    Div editorDiv = new Div();
    editorDiv.setClassName("editor");
    editorLayoutDiv.add(editorDiv);

    FormLayout formLayout = new FormLayout();
    inputFirstName = new TextField("First Name");
    inputLastName = new TextField("Last Name");
    inputPhoneNumber = new TextField("Phone Number");
    inputEmail = new TextField("Email");
    inputOrderDescription = new TextArea("Order Description");
    inputMeasurements = new TextArea("Measurements");
    inputSubtotal = new TextField("Subtotal");
    inputTotal = new TextField("Total");
    inputDate = new DatePicker("Date");
    inputComplete = new Checkbox("Complete");
    Component[] fields = new Component[] { inputFirstName, inputLastName, inputPhoneNumber, inputEmail, inputOrderDescription, inputMeasurements,
        inputSubtotal, inputTotal, inputDate, inputComplete };

    formLayout.add(fields);
    
    // Create editor header row.
    HorizontalLayout editorHeader = new HorizontalLayout();
    editorHeader.setAlignItems(Alignment.CENTER);
    // Style hide button.
    buttonHideSidebar.setWidth("100px");
    buttonHideSidebar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    // Add avatar group & hide button to header row.
    editorHeader.add(avatarGroup, buttonHideSidebar);
    // Add header row, title, and form to editor div.
    editorDiv.add(editorHeader, editTitle, formLayout);
    
    createButtonLayout(editorLayoutDiv);
    splitLayout.addToSecondary(editorLayoutDiv);
  }

  private void createButtonLayout(Div editorLayoutDiv) {
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setClassName("button-layout");
    buttonCancel.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
    buttonLayout.getStyle().set("background-color", "black");
    buttonLayout.getStyle().set("opacity", "0.7");
    buttonLayout.add(buttonSave, buttonCancel);
    editorLayoutDiv.add(buttonLayout);
  }

  private void createGridLayout(SplitLayout splitLayout) {
    Div wrapper = new Div();
    wrapper.setClassName("grid-wrapper");
    splitLayout.addToPrimary(wrapper);
    wrapper.add(grid);
  }

  private void refreshGrid() {
    grid.select(null);
//        grid.getLazyDataView().refreshAll();
    grid.getGenericDataView().refreshAll();
  }

  private void clearForm() {
    populateForm(null);
  }

  private void populateForm(TrampolineOrder value) {
    this.trampolineOrder = value;
    String topic = null;
    if (this.trampolineOrder != null && this.trampolineOrder.getId() != null) {
      topic = "trampolineOrder/" + this.trampolineOrder.getId();
//            avatarGroup.getStyle().set("visibility", "visible");
      editTitle.setText("Edit Order");
      currentActionCategory = "Edited Order";
      currentActionDetails = " edited order #";
    } else {
//            avatarGroup.getStyle().set("visibility", "hidden");
      editTitle.setText("New Order");
      currentActionCategory = "Created Order";
      currentActionDetails = " created order #";
    }
    binder.setTopic(topic, () -> this.trampolineOrder);
    avatarGroup.setTopic(topic);
  }
}