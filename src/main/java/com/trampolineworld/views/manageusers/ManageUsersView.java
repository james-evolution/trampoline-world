package com.trampolineworld.views.manageusers;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.LogEntry;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.LogEntryRepository;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.data.service.WebhookRepository;
import com.trampolineworld.views.MainLayout;
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
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("serial")
@PageTitle("Manage Users")
@Route(value = "users/:userID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({ "ADMIN" })
@Uses(Icon.class)
@CssImport(themeFor = "vaadin-grid", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(value = "./themes/trampolineworld/views/dialog.css", themeFor = "vaadin-dialog-overlay")
public class ManageUsersView extends Div implements BeforeEnterObserver {

  private final String DB_URL = System.getenv("spring.datasource.url");
  private final String USER = System.getenv("spring.datasource.username");
  private final String PASS = System.getenv("spring.datasource.password");

  private String currentActionCategory;
  private String currentActionDetails;

  private final String USER_ID = "userID";
  private final String USER_EDIT_ROUTE_TEMPLATE = "users/%s/edit";
  private UUID targetId;
  private String targetName;

  private User userToEdit;
  private boolean isNewUser = false;

  private Grid<User> grid = new Grid<>(User.class, false);
  private H2 editTitle;

  private TextField inputSearchFilter = new TextField();
  private TextField inputID, inputUsername, inputDisplayName, inputEmail, inputProfilePictureUrl;
  private PasswordField inputHashedPassword;

  private CheckboxGroup<Role> inputRoles;
  private Select<Integer> inputColorIndex;

  private Div editorLayoutDiv;
  private Div editorDiv;
  private FormLayout formLayout;
  private HorizontalLayout buttonHeaderContainer = new HorizontalLayout();

  private Dialog confirmDeleteDialog = new Dialog();

  private Button buttonCancel = new Button("Cancel");
  private Button buttonSave = new Button("Save");
  private Button buttonNewUser = new Button("New User");
  private Button buttonHideSidebar = new Button("Cancel");

  private User user;
  private final LogEntryRepository logEntryRepository;
  private final WebhookRepository webhookRepository;
  private final UserService userService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private User currentUser;
  private User createdUser;

  private Grid.Column<User> columnId, columnUsername, columnDisplayName, columnRoles, columnEmail,
      columnProfilePictureUrl, columnColorIndex;
  private GridContextMenu<User> menu;

  private List<UUID> uuidPriorityOptions = new ArrayList<>(Arrays.asList(
      UUID.fromString("11c0475e-e6c4-4885-ab3b-933afc925831"), // Ozzy
      UUID.fromString("c809eeaf-7624-4152-8c5c-2854ca4019ba"), // Bear
      UUID.fromString("e7f5d385-b7c6-49d6-9fc2-56b264fa2796"), // TW Admin
      UUID.fromString("52d1376a-b8d4-4d60-874e-b804d078f780"), // TW Tech
      UUID.fromString("275626f5-d4a0-4ce7-b90f-f34e236b8c6f"), // TW User
      UUID.fromString("830b6bf0-d783-491c-b7e1-98e219548ab8"), // Sneaky Rat
      UUID.fromString("f60e2718-0600-48a9-849f-20ee79ebdf45"),
      UUID.fromString("f56e5fd7-4bbc-4d8e-890b-affadbc35d78"), 
      UUID.fromString("e761fb9a-5e84-4854-ab7c-aa9551791a40"),
      UUID.fromString("e6414878-931c-43ed-b76d-8b37462fb6d6"), 
      UUID.fromString("d91687ed-7b89-462b-9f4a-8d5c3b5430dd"),
      UUID.fromString("d1168d1a-1b86-40ac-b828-8dc6a7e43318"), 
      UUID.fromString("bc623979-77c1-4a36-80a6-a6eca6cd8ca3"),
      UUID.fromString("a42449a0-da4e-4a3e-b9ae-ae2729869699"), 
      UUID.fromString("a236363d-dfa1-4a7e-8e41-8cb787e0ed57"),
      UUID.fromString("88518b49-916d-4c08-a3b3-a1d85c92939b"), 
      UUID.fromString("5c2f378b-7667-447a-a996-d8360f6a5451"),
      UUID.fromString("5c17735c-774b-4c59-ad49-db3829a4d218"),
      UUID.fromString("016c6be5-9926-474a-a555-1ea24bfdd7ea"),
      UUID.fromString("59469b67-fe3a-42cb-b8c1-9d0d4327d5d6")));

  @Autowired
  public ManageUsersView(LogEntryRepository logEntryRepository, WebhookRepository webhookRepository,
      UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.logEntryRepository = logEntryRepository;
    this.webhookRepository = webhookRepository;
    this.userService = userService;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    addClassNames("trampoline-orders-view");

    String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
    currentUser = userRepository.findByUsername(currentUsername);

    // Create split-view UI
    SplitLayout splitLayout = new SplitLayout();
    splitLayout.getStyle().set("margin-top", "0px !important");
    splitLayout.getStyle().set("border-style", "solid");
    splitLayout.getStyle().set("border-top", "1px");
    splitLayout.getStyle().set("border-bottom", "0px");
    splitLayout.getStyle().set("border-left", "0px");
    splitLayout.getStyle().set("border-right", "0px");
    splitLayout.getStyle().set("border-color", "rgb(5, 57, 54)");

    // Create grid and editor layouts
    createGridLayout(splitLayout);
    createEditorLayout(splitLayout);

    // Configure the grid.
    configureGrid(userService, splitLayout);

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

    // Default user is new.
    userToEdit = new User();
    userToEdit.setHashedPassword(passwordEncoder.encode("user"));

    // Configure the form.
//    configureFormBindings(userInfo);
    configureFormButtons(userService);
  }

  private void configureDeleteDialog() {
//    confirmDeleteDialog.setHeaderTitle("Delete " + targetName);
    confirmDeleteDialog.setDraggable(true);
    confirmDeleteDialog.addClassName("deleteDialog");

    VerticalLayout dialogLayout = createDialogLayout();
    confirmDeleteDialog.add(dialogLayout);

    Button dialogDeleteUserButton = createDialogDeleteOrderButton(confirmDeleteDialog);
    Button cancelButton = new Button("Cancel", e -> confirmDeleteDialog.close());
    confirmDeleteDialog.getFooter().add(dialogDeleteUserButton);
    confirmDeleteDialog.getFooter().add(cancelButton);
  }

  private void createContextMenu() {
    // Add the context menu to the grid.
    menu = grid.addContextMenu();

    // Listen for the event in which the context menu is opened, then save the id of
    // the user that was right clicked on.
    menu.addGridContextMenuOpenedListener(event -> {
      targetId = event.getItem().get().getId();
      targetName = event.getItem().get().getUsername() + " (" + event.getItem().get().getDisplayName() + ")";
      confirmDeleteDialog.setHeaderTitle("Delete " + targetName);
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

  private static VerticalLayout createDialogLayout() {

    Paragraph confirmationMessage = new Paragraph("Are you sure you want to delete this user?");
    VerticalLayout dialogLayout = new VerticalLayout(confirmationMessage);
    dialogLayout.setPadding(false);
    dialogLayout.setSpacing(false);
    dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

    return dialogLayout;
  }

  private Button createDialogDeleteOrderButton(Dialog dialog) {
    Button dialogDeleteButton = new Button("Delete", e -> {
      try {
        Optional<User> userToDelete = userRepository.findById(targetId);

        // Get user name.
        String userName = userToDelete.get().getUsername() + " (" + userToDelete.get().getDisplayName() + ")";
        // Delete user from database.
        userService.delete(targetId);

        // Log action.
        currentActionCategory = "Deleted User";
        currentActionDetails = " deleted user " + targetId.toString() + " named " + userName;
        new LogEntry(logEntryRepository, webhookRepository, currentUser.getId(),
            currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")", targetId.toString(), userName,
            currentActionCategory,
            currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")" + currentActionDetails);
        // Notify of deletion success.
        Notification.show("User deleted.", 4000, Position.TOP_CENTER)
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

  private void configureFormButtons(UserService userService) {
    // When the cancel button is clicked, clear the form and refresh the grid.
    buttonCancel.addClickListener(e -> {
      clearForm();
      updateGrid();
      editorLayoutDiv.setVisible(false);
      buttonHideSidebar.setVisible(false);
    });

    // When the save button is clicked, save the new user.
    buttonSave.addClickListener(e -> {
      try {
        // Pull data from form, update user object, update repository.
        updateUserFromForm();

        // Log action.
        if (currentActionCategory == "Created User") {
          new LogEntry(logEntryRepository, webhookRepository, currentUser.getId(),
              currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")", createdUser.getId(),
              currentActionCategory, currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")"
                  + currentActionDetails + " " + createdUser.getId().toString());
        } else if (currentActionCategory == "Edited User") {
          new LogEntry(logEntryRepository, webhookRepository, currentUser.getId(),
              currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")", this.user.getId(),
              currentActionCategory, currentUser.getUsername() + " (" + currentUser.getDisplayName() + ")"
                  + currentActionDetails + " " + this.user.getId().toString());
        }

        // Clear form, update grid.
        clearForm();
        updateGrid();
        // Hide editor & hide 'hide' button.
        editorLayoutDiv.setVisible(false);
        buttonHideSidebar.setVisible(false);
        // Notify of success.
        Notification.show("User details stored.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate(ManageUsersView.class);
        // Notify of failure.
      } catch (Exception exception) {
        Notification.show("An error occurred.", 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        exception.printStackTrace();
      }
    });
  }

  private void updateUserFromForm() {

    // Check if this is a new user, if it is, create a new user object & use the
    // default encoded password.
    if (isNewUser) {
      User newUser = new User();
      newUser.setUsername(inputUsername.getValue());
      newUser.setDisplayName(inputDisplayName.getValue());
      newUser.setEmail(inputEmail.getValue());
      newUser.setRoles(inputRoles.getValue());
      newUser.setColorIndex(inputColorIndex.getValue() == null ? 1 : inputColorIndex.getValue());
      newUser.setProfilePictureUrl(inputProfilePictureUrl.getValue());
      newUser.setHashedPassword(passwordEncoder.encode("user"));
//      userService.update(newUser);
      userRepository.save(newUser);
      /*
       * When a user is saved to the repository a new UUID is generated for it by
       * Hibernate. This means we cannot update the UUID until after it's been saved
       * at least once. So, -now- after saving it to the repository, we update the ID.
       */
      updateToPriorityID(newUser);

      createdUser = userRepository.findByUsername(inputUsername.getValue());

      currentActionCategory = "Created User";
      currentActionDetails = " created an account for " + createdUser.getUsername() + " ("
          + createdUser.getDisplayName() + ")";
    } 
    // Else, if this is not a new user, set its attributes equal to those in the form fields.
    else {
      this.user.setUsername(inputUsername.getValue());
      this.user.setDisplayName(inputDisplayName.getValue());
      this.user.setEmail(inputEmail.getValue());
      this.user.setRoles(inputRoles.getValue());
      this.user.setColorIndex(inputColorIndex.getValue() == null ? 1 : inputColorIndex.getValue());
      this.user.setProfilePictureUrl(inputProfilePictureUrl.getValue());
      // If password field is empty or null, do nothing. Leave it as is.
      if (inputHashedPassword.getValue().isEmpty() || inputHashedPassword.getValue() == null) {
      }
      // Else, encode the new password.
      else {
        this.user.setHashedPassword(passwordEncoder.encode(inputHashedPassword.getValue()));
      }

      currentActionCategory = "Edited User";
      currentActionDetails = " edited the account for " + this.user.getUsername() + " (" + this.user.getDisplayName()
          + ")";
      userService.update(this.user);
    }
  }

  /*
   * This method will attempt to assign a UUID to the user from a prioritized list
   * of 20 UUIDs that are already registered with the CollaborationEngine. This is
   * intended to prevent us from unnecessarily surpassing the 20 user / month
   * quota. Ideally, this ID should be one that is registered but not currently
   * taken by an existing user. If all 20 IDs in the priority list are already
   * assigned to users in the system, we will instead leave this User with their
   * default, auto-generated UUID, which will count towards the quota.
   */
  private void updateToPriorityID(User newUser) {
    // Get all users, create empty list to store their IDs.
    List<User> allUsers = userService.findAllNoFilter();
    List<UUID> allUserIDs = new ArrayList<UUID>();
    // Loop through all users, add their IDs to the ID list.
    for (User u : allUsers) {
      allUserIDs.add(u.getId());
    }
    System.out.println("\n\n---------------------------");
    System.out.println(allUserIDs);
    // Loop through all user IDs and remove them from the priority options list
    // (We don't want to pick an ID that's already taken)
    for (UUID u : allUserIDs) {
      if (uuidPriorityOptions.contains(u)) {
        uuidPriorityOptions.remove(u);
      }
    }

    System.out.println("Priority ID options left: " + String.valueOf(uuidPriorityOptions.size()));

    /*
     * If there are still priority options left in the options list, use one of those.
     */
    if (uuidPriorityOptions.size() > 0) {
      // Randomly select a new ID from the priority options list.
      Random rand = new Random();
      UUID newID = uuidPriorityOptions.get(rand.nextInt(uuidPriorityOptions.size()));
      /*
       * UPDATE ID IN THE DATABASE VIA SQL UPDATE STATEMENT.
       * This cannot be done by Hibernate or JPA because they don't allow us to change the values of primary keys.
       * While typically bad practice, this is a rare scenario in which it's necessary for licensing purposes.
       */
         // Open a connection
         try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
         ) {         
            String sql = "UPDATE application_user " +
               "SET id = \"" + newID + "\" WHERE username = \"" + newUser.getUsername() + "\";";
            stmt.executeUpdate(sql);
         } catch (SQLException e) {
            e.printStackTrace();
         } 
      System.out.println("User created with priority ID: " + newID);
      Notification.show("User created with priority ID: " + newID, 8000, Position.TOP_CENTER)
      .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    /*
     * If there are NO PRIORITY OPTIONS LEFT, do nothing.
     * Allow the User to keep their hibernate generated UUID.
     */
    else {
      System.out.println("No priority IDs available. Using generated ID: " + newUser.getId().toString());
      Notification.show("No priority IDs available. User created with generated ID: " + newUser.getId().toString(), 8000, Position.TOP_CENTER)
      .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      // Do nothing. Keep the ID as is.
    }
    System.out.println("---------------------------\n\n");
  }

  private static Component createHeaderRoles() {
    Span span = new Span("Roles");
    Icon icon = VaadinIcon.USER_STAR.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)").set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.getElement().setAttribute("title",
        "Admins have all permissions but cannot see the Debug page. Techs can see the debug page. \nUsers can only see the orders page and the chat, they cannot delete orders.");
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }

  private static Component createHeaderEmail() {
    Span span = new Span("Email");
    Icon icon = VaadinIcon.ENVELOPE.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)").set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.getElement().setAttribute("title",
        "Determines where a user's reset code will be sent if they forget their password.");
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }

  private static Component createHeaderProfileUrl() {
    Span span = new Span("Profile Picture URL");
    Icon icon = VaadinIcon.LINK.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)").set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.getElement().setAttribute("title", "Please ensure the URL ends in .jpg, .png, .gif, or .webp");
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }

  private static Component createHeaderColorIndex() {
    Span span = new Span("Color Index");
    Icon icon = VaadinIcon.PALETTE.create();
    icon.getStyle().set("height", "var(--lumo-font-size-m)").set("color", "var(--lumo-contrast-70pct)");
    icon.getStyle().set("margin-right", "4px");
    HorizontalLayout layout = new HorizontalLayout(icon, span);
    layout.getElement().setAttribute("title",
        "Determines what color frames a user's profile picture and the fields they're editing.");
    layout.setAlignItems(Alignment.AUTO);
    layout.setSpacing(false);
    return layout;
  }

  private void configureGrid(UserService userService, SplitLayout splitLayout) {
    grid.setColumnReorderingAllowed(true);
    grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

    // Add columns to the grid.
    columnId = grid.addColumn("id").setAutoWidth(true).setResizable(true).setHeader("User ID");
    columnUsername = grid.addColumn("username").setAutoWidth(true).setResizable(true);
    columnDisplayName = grid.addColumn("displayName").setAutoWidth(true).setResizable(true);
    columnRoles = grid.addColumn(createStatusComponentRenderer()).setAutoWidth(true).setResizable(true)
        .setHeader(createHeaderRoles());
    columnEmail = grid.addColumn("email").setAutoWidth(true).setResizable(true).setHeader(createHeaderEmail());
    columnProfilePictureUrl = grid.addColumn("profilePictureUrl").setWidth("300px").setResizable(true)
        .setHeader(createHeaderProfileUrl());
    columnColorIndex = grid.addColumn("colorIndex").setAutoWidth(true).setResizable(true)
        .setHeader(createHeaderColorIndex());

    columnId.setVisible(false);
    columnUsername.setVisible(false);

    updateGrid();

    // When a row is selected or deselected, populate form.
    grid.asSingleSelect().addValueChangeListener(event -> {
      if (event.getValue() != null) {
        // Set user to selected user.
        userToEdit = event.getValue();
        // Show layout & hide button.
        editorLayoutDiv.setVisible(true);
        buttonHideSidebar.setVisible(true);
        splitLayout.setSplitterPosition(0);
        UI.getCurrent().navigate(String.format(USER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
      } else {
        // Hide layout & clear form.
        editorLayoutDiv.setVisible(false);
        clearForm();
        UI.getCurrent().navigate(ManageUsersView.class);
      }
    });
  }

  private void updateGrid() {
    grid.setItems(userService.findAll(inputSearchFilter.getValue()));
  }

  // Generating icons based upon role.
  private static Icon createIcon(String roleName) {
    Icon icon = VaadinIcon.USER.create();

    if (roleName == "ADMIN") {
      icon = VaadinIcon.USER_STAR.create();
    } else if (roleName == "TECH") {
      icon = VaadinIcon.DESKTOP.create();
    } else if (roleName == "USER") {
      icon = VaadinIcon.USER.create();
    }
    icon.getStyle().set("padding", "var(--lumo-space-xs");

    return icon;
  }

  // Logic for generating role badges.
  private static final SerializableBiConsumer<Span, User> statusComponentUpdater = (span, user) -> {
    Set<Role> roles = user.getRoles();
    for (Role r : roles) {
      String roleName = r.toString();
      Span badge = new Span(createIcon(roleName), new Span(roleName));
      badge.getElement().getStyle().set("margin", "3px");
      if (roleName == "ADMIN") {
        badge.getElement().getThemeList().add("badge success");
      } else if (roleName == "TECH") {
        badge.getElement().getThemeList().add("badge");
      } else if (roleName == "USER") {
        badge.getElement().getThemeList().add("badge contrast");
      }
      span.add(badge);
    }
  };

  // For rendering a more complex component in a grid cell.
  private static ComponentRenderer<Span, User> createStatusComponentRenderer() {
    return new ComponentRenderer<>(Span::new, statusComponentUpdater);
  }

  private void createButtonHeader(SplitLayout splitLayout) {
    // Configure button header container.
    buttonHeaderContainer.setSpacing(false);
    buttonHeaderContainer.setAlignItems(Alignment.BASELINE);

    buttonNewUser.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    buttonNewUser.getElement().getStyle().set("margin-left", "6px");
    buttonNewUser.getElement().getStyle().set("margin-right", "6px");
    buttonNewUser.addClickListener(e -> {
      // Clear form contents & update grid data.
      clearForm();
      updateGrid();
      // Show editor layout, show hide button.
      editorLayoutDiv.setVisible(true);
      buttonHideSidebar.setVisible(true);
      // Full screen the editor.
      splitLayout.setSplitterPosition(0);
    });

//    buttonHideSidebar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
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
    inputSearchFilter.setHelperText("Filter by name or email");
    inputSearchFilter.setClearButtonVisible(true);
    inputSearchFilter.setValueChangeMode(ValueChangeMode.LAZY); // Don't hit database on every keystroke. Wait for
    inputSearchFilter.addValueChangeListener(e -> updateGrid());

    Button menuButton = new Button("Show/Hide Columns");
    menuButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    menuButton.getStyle().set("margin-right", "6px");
    menuButton.getElement().setAttribute("title", "There are additional column options, click me to reveal them!");

    ColumnToggleContextMenu columnToggleContextMenu = new ColumnToggleContextMenu(menuButton);
    columnToggleContextMenu.addColumnToggleItem("ID", columnId);
    columnToggleContextMenu.addColumnToggleItem("Username", columnUsername);
    columnToggleContextMenu.addColumnToggleItem("Display Name", columnDisplayName);
    columnToggleContextMenu.addColumnToggleItem("Roles", columnRoles);
    columnToggleContextMenu.addColumnToggleItem("Email", columnEmail);
    columnToggleContextMenu.addColumnToggleItem("Profile Picture Url", columnProfilePictureUrl);
    columnToggleContextMenu.addColumnToggleItem("Color Index", columnColorIndex);

//    buttonHeaderContainer.add(menuButton, inputSearchFilter, buttonNewUser, buttonHideSidebar);
    buttonHeaderContainer.getStyle().set("margin-bottom", "0px !important");
    buttonHeaderContainer.add(menuButton, inputSearchFilter, buttonNewUser);
//    buttonHeaderContainer.setAlignItems(Alignment.BASELINE);
  }

  private static class ColumnToggleContextMenu extends ContextMenu {
    public ColumnToggleContextMenu(Component target) {
      super(target);
      setOpenOnClick(true);
    }

    void addColumnToggleItem(String label, Grid.Column<User> column) {
      MenuItem menuItem = this.addItem(label, e -> {
        column.setVisible(e.getSource().isChecked());
      });
      menuItem.setCheckable(true);
      menuItem.setChecked(column.isVisible());
    }
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    Optional<UUID> userID = event.getRouteParameters().get(USER_ID).map(UUID::fromString);
    if (userID.isPresent()) {
      Optional<User> userFromBackend = userService.get(userID.get());
      if (userFromBackend.isPresent()) {
        populateForm(userToEdit);
      } else {
        Notification
            .show(String.format("The requested user was not found, ID = %d", userID.get()), 4000, Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        // When a row is selected but the data is no longer available, update the grid
        // component.
        updateGrid();
        event.forwardTo(ManageUsersView.class);
      }
    }
  }

  private void createEditorLayout(SplitLayout splitLayout) {
    editorLayoutDiv = new Div();
    editorDiv = new Div();
    formLayout = new FormLayout();

    editTitle = new H2("New User");
    editorLayoutDiv.setClassName("editor-layout");
    editorLayoutDiv.setVisible(false);

    editorDiv.setClassName("editor");
    editorLayoutDiv.add(editorDiv);
    
    inputID = new TextField("ID");
    inputID.setReadOnly(true);
    inputID.setHelperText("This field is read-only and cannot be changed.");

    inputUsername = new TextField("Username");
    inputUsername.setHelperText(
        "Your username is the one you log in with. This is not to be confused with the display name, which is simply for aesthetics.");

    inputDisplayName = new TextField("Display Name");
    inputDisplayName.setHelperText(
        "Your display name is essentially your nickname. It's what will be displayed to other users in the system. It's a good idea to have a different display name than username, that way people don't know what username to type if they attempt to log into your account.");

    inputEmail = new TextField("Email");
    inputEmail.setHelperText(
        "An email is not required, but if you ever forget your password, this is where your reset link & code will be sent.");

    inputRoles = new CheckboxGroup<>();
    inputRoles.setLabel("Roles");
    inputRoles.setItems(Role.ADMIN, Role.TECH, Role.USER);
    inputRoles.setHelperText(
        "Admins have all permissions but cannot see the debug page. Techs can see the debug page. Users can only access the orders page and the chat. They cannot delete orders, but they can add, edit, and view them.");

    inputColorIndex = new Select<>();
    inputColorIndex.setLabel("Color Index");
    inputColorIndex.setItems(0, 1, 2, 3, 4, 5, 6, 7);
    inputColorIndex.setHelperText("[0=None] [1=Pink] [2=Purple] [3=Green] [4=Orange] [5=Magenta] [6=Cyan] [7=Yellow]");

    inputHashedPassword = new PasswordField();
    inputHashedPassword.setLabel("Password");
    inputHashedPassword.setHelperText(
        "When creating a new user, leave this field blank if you wish to use the default password, 'user'. When editing existing users, leave this field blank to leave their password as is. WARNING: Do not change the value of this field if you don't want to change a user's password. It will get encrypted.");
    inputHashedPassword.setValueChangeMode(ValueChangeMode.LAZY);

    inputProfilePictureUrl = new TextField("Profile Picture URL");
    inputProfilePictureUrl.setHelperText(
        "File uploads are not yet supported for profile pictures. You can, however, pass in an image URL. Right click on an image from the net and select 'Copy Image Address' and then paste it here. The url path must end in .jpg, .png, .gif, or .webp");

    Component[] fields = new Component[] { inputUsername, inputDisplayName, inputEmail, inputRoles, inputHashedPassword,
        inputProfilePictureUrl, inputColorIndex, inputID};

    formLayout.add(fields);

//    editorDiv.add(editTitle, formLayout);

    // Create editor header row.
    HorizontalLayout editorHeader = new HorizontalLayout();
    editorHeader.setAlignItems(Alignment.BASELINE);
    // Style hide button.
    buttonHideSidebar.setWidth("100px");
    buttonHideSidebar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    editTitle.setWidth("100%");
    // Add avatar group & hide button to header row.
    editorHeader.add(editTitle, buttonHideSidebar);

    // Add header row, title, and form to editor div.
    editorDiv.add(editorHeader, formLayout);

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

  private void clearForm() {
    populateForm(null);
  }

  private void populateForm(User value) {
    this.user = value;

    if (this.user != null && this.user.getId() != null) {
      editTitle.setText("Edit User");
      isNewUser = false;
      // Populate form data here.
      inputID.setValue(value.getId().toString());
      inputUsername.setValue(value.getUsername() == null ? "" : value.getUsername());
      inputDisplayName.setValue(value.getDisplayName() == null ? "" : value.getDisplayName());
      inputEmail.setValue(value.getEmail() == null ? "" : value.getEmail());
      inputRoles.setValue(value.getRoles());
      inputColorIndex.setValue(value.getColorIndex() == null ? 1 : value.getColorIndex());
      inputProfilePictureUrl.setValue(value.getProfilePictureUrl() == null ? "" : value.getProfilePictureUrl());
    } else {
      // Clear form data here.
      editTitle.setText("New User");
      isNewUser = true;
      inputID.clear();
      inputUsername.clear();
      inputDisplayName.clear();
      inputEmail.clear();
      inputRoles.clear();
      inputColorIndex.clear();
      inputProfilePictureUrl.clear();
    }

  }
}