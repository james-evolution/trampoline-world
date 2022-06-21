package com.trampolineworld.views.trampolineordersreadonly;

import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.TrampolineOrderService;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
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
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.icon.Icon;
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
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("All Orders")
@Route(value = "trampoline_orders_read/:trampolineOrderID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({"USER"})
@Uses(Icon.class)
@CssImport(themeFor = "vaadin-grid", value = "./themes/trampolineworld/views/grid-theme.css")
@CssImport(value = "./themes/trampolineworld/views/dialog.css", themeFor = "vaadin-dialog-overlay")
public class TrampolineOrdersReadOnlyView extends Div implements BeforeEnterObserver {

	public static String username = "";

	private final String TRAMPOLINEORDER_ID = "trampolineOrderID";
	private final String TRAMPOLINEORDER_EDIT_ROUTE_TEMPLATE = "trampoline_orders_read/%s/edit";
	private final String TRAMPOLINEORDER_VIEW_ROUTE_TEMPLATE = "view_order/%s";
	private Long targetId;
	private Grid<TrampolineOrder> grid = new Grid<>(TrampolineOrder.class, false);
	CollaborationAvatarGroup avatarGroup;
	H2 editTitle;
	private TextField filterTextField = new TextField();
	private Checkbox complete;
	private TextField firstName, lastName, phoneNumber, email, subtotal, total;
	private TextArea orderDescription, measurements;
	private DatePicker date;

	private GridContextMenu<TrampolineOrder> menu;
	private Div editorLayoutDiv;
	private HorizontalLayout buttonHeaderContainer = new HorizontalLayout();

	private Dialog confirmDeleteDialog = new Dialog();

	private Button cancel = new Button("Cancel");
	private Button save = new Button("Save");
	private Button newOrderButton = new Button("New Order");
	private Button hideSidebarButton = new Button("Hide");
	
	private CollaborationBinder<TrampolineOrder> binder;
	private TrampolineOrder trampolineOrder;
	private final TrampolineOrderService trampolineOrderService;
	private final UserService userService;
	private final UserRepository userRepository;

	@Autowired
	public TrampolineOrdersReadOnlyView(TrampolineOrderService trampolineOrderService, UserService userService, UserRepository userRepository) {
		this.trampolineOrderService = trampolineOrderService;
		this.userService = userService;
		this.userRepository = userRepository;
		addClassNames("trampoline-orders-view");

//    	Notification.show("Welcome, " + username, 4000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

		String currentUsername = VaadinRequest.getCurrent().getUserPrincipal().getName();
		User currentUser = userRepository.findByUsername(currentUsername);

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

		// Configure avatar group (users / pfps)
		avatarGroup = new CollaborationAvatarGroup(userInfo, null);
		avatarGroup.getStyle().set("visibility", "visible");

		// Create grid and editor layouts.
		createGridLayout(splitLayout);
		createEditorLayout(splitLayout);

		// Create button header bar.
		createButtonHeader(splitLayout); // Requires splitLayout argument to define button functions.

		// Add buttonHeaderContainer and splitLayout to view.
		add(buttonHeaderContainer);
		add(splitLayout);

		// Create context menu.
		createContextMenu(trampolineOrderService); // View & Delete buttons.

		// Configure the grid.
		configureGrid(trampolineOrderService, splitLayout);

		// Configure & add delete confirmation dialog.
		configureDeleteDialog(trampolineOrderService);
		add(confirmDeleteDialog);

		// Configure the form.
		configureForm(userInfo);
		configureFormButtons(trampolineOrderService);
	}

	private void configureDeleteDialog(TrampolineOrderService trampolineOrderService) {
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

	private void configureFormButtons(TrampolineOrderService trampolineOrderService) {
		// When the cancel button is clicked, clear the form and refresh the grid.
		cancel.addClickListener(e -> {
			clearForm();
//            refreshGrid();
			updateGrid();
			editorLayoutDiv.setVisible(false);
			hideSidebarButton.setVisible(false);
		});

		// When the save button is clicked, save the new order.
		save.addClickListener(e -> {
			try {
				if (this.trampolineOrder == null) {
					this.trampolineOrder = new TrampolineOrder();
				}
				binder.writeBean(this.trampolineOrder);

				trampolineOrderService.update(this.trampolineOrder);
				clearForm();
				updateGrid();
				editorLayoutDiv.setVisible(false);
				hideSidebarButton.setVisible(false);
				Notification.show("Order details stored.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				UI.getCurrent().navigate(TrampolineOrdersReadOnlyView.class);
			} catch (ValidationException validationException) {
				Notification.show("Invalid form input.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_ERROR);
			}
		});
	}

	private void configureForm(UserInfo userInfo) {
		binder = new CollaborationBinder<>(TrampolineOrder.class, userInfo);
		// Bind fields. This is where you'd define e.g. validation rules
		binder.forField(subtotal, String.class).asRequired("Subtotal field cannot be empty.")
				.withConverter(new StringToDoubleConverter("Only numbers are allowed")).bind("subtotal");
		binder.forField(total, String.class).asRequired("Total field cannot be empty.")
				.withConverter(new StringToDoubleConverter("Only numbers are allowed")).bind("total");
		binder.forField(date, LocalDate.class).asRequired("Date field cannot be empty.").bind("date");
		binder.bindInstanceFields(this);
	}

	private void configureGrid(TrampolineOrderService trampolineOrderService, SplitLayout splitLayout) {
		grid.setColumnReorderingAllowed(true);
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
//		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		
		// Add columns to the grid.
		grid.addColumn("complete").setAutoWidth(true).setResizable(true);
		grid.addColumn("firstName").setAutoWidth(true).setResizable(true);
		grid.addColumn("lastName").setAutoWidth(true).setResizable(true);
		grid.addColumn("phoneNumber").setAutoWidth(true).setResizable(true);
		grid.addColumn("email").setAutoWidth(true).setResizable(true);
		grid.addColumn("orderDescription").setWidth("300px").setResizable(true);
		grid.addColumn("measurements").setWidth("300px").setResizable(true);
		grid.addColumn("subtotal").setAutoWidth(true).setResizable(true);
		grid.addColumn("total").setAutoWidth(true).setResizable(true);
		grid.addColumn("date").setAutoWidth(true).setResizable(true);
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
				hideSidebarButton.setVisible(true);
				splitLayout.setSplitterPosition(0);
				UI.getCurrent().navigate(String.format(TRAMPOLINEORDER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			} else {
				editorLayoutDiv.setVisible(false);
				clearForm();
				UI.getCurrent().navigate(TrampolineOrdersReadOnlyView.class);
			}
		});
	}

	private void updateGrid() {
		grid.setItems(trampolineOrderService.findAll(filterTextField.getValue()));
	}

	private void createContextMenu(TrampolineOrderService trampolineOrderService) {
		// Add the context menu to the grid.
		menu = grid.addContextMenu();

		// Listen for the event in which the context menu is opened, then save the id of
		// the TrampolineOrder that was right clicked on.
		menu.addGridContextMenuOpenedListener(event -> {
			targetId = event.getItem().get().getId();
		});
		// Add menu items to the grid, send user to the 'ViewSingleOrder' page with the
		// TrampolineOrder id as an argument.
		menu.addItem("View", event -> {
			UI.getCurrent().navigate(String.format(TRAMPOLINEORDER_VIEW_ROUTE_TEMPLATE, targetId.toString()));
		});
	}

	private void createButtonHeader(SplitLayout splitLayout) {
		// Configure button header container.
		buttonHeaderContainer.setSpacing(false);
		buttonHeaderContainer.setAlignItems(Alignment.BASELINE);

		newOrderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		newOrderButton.getElement().getStyle().set("margin-left", "6px");
		newOrderButton.getElement().getStyle().set("margin-right", "6px");
		newOrderButton.addClickListener(e -> {
			clearForm();
			updateGrid();
			editorLayoutDiv.setVisible(true);
			hideSidebarButton.setVisible(true);
			splitLayout.setSplitterPosition(0);
		});

		hideSidebarButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		hideSidebarButton.getElement().getStyle().set("margin-left", "0px !important");
		hideSidebarButton.getElement().getStyle().set("margin-right", "6px");
		hideSidebarButton.setVisible(false);
		
		hideSidebarButton.addClickListener(e -> {
			clearForm();
			updateGrid();
			editorLayoutDiv.setVisible(false);
			hideSidebarButton.setVisible(false);
		});

		filterTextField.setPlaceholder("Search...");
		filterTextField.setHelperText("Filter by name, email, or number");
		filterTextField.setClearButtonVisible(true);
		filterTextField.setValueChangeMode(ValueChangeMode.LAZY); // Don't hit database on every keystroke. Wait for
																	// user to finish typing.
		filterTextField.addValueChangeListener(e -> updateGrid());

		buttonHeaderContainer.add(filterTextField, newOrderButton, hideSidebarButton);
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
				event.forwardTo(TrampolineOrdersReadOnlyView.class);
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
				trampolineOrderService.delete(targetId);
				Notification.show("Order deleted.", 4000, Position.TOP_CENTER)
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
		firstName = new TextField("First Name");
		lastName = new TextField("Last Name");
		phoneNumber = new TextField("Phone Number");
		email = new TextField("Email");
		orderDescription = new TextArea("Order Description");
		measurements = new TextArea("Measurements");
		subtotal = new TextField("Subtotal");
		total = new TextField("Total");
		date = new DatePicker("Date");
		complete = new Checkbox("Complete");
		Component[] fields = new Component[] { firstName, lastName, phoneNumber, email, orderDescription, measurements,
				subtotal, total, date, complete };

		formLayout.add(fields);
		editorDiv.add(avatarGroup, editTitle, formLayout);
		createButtonLayout(editorLayoutDiv);

		splitLayout.addToSecondary(editorLayoutDiv);
	}

	private void createButtonLayout(Div editorLayoutDiv) {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonLayout.add(save, cancel);
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
		} else {
//            avatarGroup.getStyle().set("visibility", "hidden");
			editTitle.setText("New Order");
		}
		binder.setTopic(topic, () -> this.trampolineOrder);
		avatarGroup.setTopic(topic);
	}
}