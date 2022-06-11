package com.trampolineworld.views.trampolineorders;

import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.service.TrampolineOrderService;
import com.trampolineworld.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Trampoline Orders")
@Route(value = "trampoline_orders/:trampolineOrderID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class TrampolineOrdersView extends Div implements BeforeEnterObserver {

    private final String TRAMPOLINEORDER_ID = "trampolineOrderID";
    private final String TRAMPOLINEORDER_EDIT_ROUTE_TEMPLATE = "trampoline_orders/%s/edit";

    private Grid<TrampolineOrder> grid = new Grid<>(TrampolineOrder.class, false);

    CollaborationAvatarGroup avatarGroup;

    private Checkbox status;
    private TextField orderId;
    private TextField firstName;
    private TextField lastName;
    private TextField phoneNumber;
    private TextField email;
    private TextField orderDescription;
    private TextField measurements;
    private TextField price;
    private TextField subtotal;
    private TextField total;
    private DatePicker date;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<TrampolineOrder> binder;

    private TrampolineOrder trampolineOrder;

    private final TrampolineOrderService trampolineOrderService;

    @Autowired
    public TrampolineOrdersView(TrampolineOrderService trampolineOrderService) {
        this.trampolineOrderService = trampolineOrderService;
        addClassNames("trampoline-orders-view");

        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "Steve Lange");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set("visibility", "hidden");

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<TrampolineOrder> statusRenderer = LitRenderer.<TrampolineOrder>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", status -> status.isStatus() ? "check" : "minus").withProperty("color",
                        status -> status.isStatus()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(statusRenderer).setHeader("Status").setAutoWidth(true);

        grid.addColumn("orderId").setAutoWidth(true);
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("phoneNumber").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("orderDescription").setAutoWidth(true);
        grid.addColumn("measurements").setAutoWidth(true);
        grid.addColumn("price").setAutoWidth(true);
        grid.addColumn("subtotal").setAutoWidth(true);
        grid.addColumn("total").setAutoWidth(true);
        grid.addColumn("date").setAutoWidth(true);
        grid.setItems(query -> trampolineOrderService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(TRAMPOLINEORDER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(TrampolineOrdersView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(TrampolineOrder.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(orderId, String.class).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("orderId");
        binder.forField(price, String.class).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("price");
        binder.forField(subtotal, String.class).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("subtotal");
        binder.forField(total, String.class).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("total");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.trampolineOrder == null) {
                    this.trampolineOrder = new TrampolineOrder();
                }
                binder.writeBean(this.trampolineOrder);

                trampolineOrderService.update(this.trampolineOrder);
                clearForm();
                refreshGrid();
                Notification.show("TrampolineOrder details stored.");
                UI.getCurrent().navigate(TrampolineOrdersView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the trampolineOrder details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> trampolineOrderId = event.getRouteParameters().get(TRAMPOLINEORDER_ID).map(UUID::fromString);
        if (trampolineOrderId.isPresent()) {
            Optional<TrampolineOrder> trampolineOrderFromBackend = trampolineOrderService.get(trampolineOrderId.get());
            if (trampolineOrderFromBackend.isPresent()) {
                populateForm(trampolineOrderFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested trampolineOrder was not found, ID = %d", trampolineOrderId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(TrampolineOrdersView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        status = new Checkbox("Status");
        orderId = new TextField("Order Id");
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        phoneNumber = new TextField("Phone Number");
        email = new TextField("Email");
        orderDescription = new TextField("Order Description");
        measurements = new TextField("Measurements");
        price = new TextField("Price");
        subtotal = new TextField("Subtotal");
        total = new TextField("Total");
        date = new DatePicker("Date");
        Component[] fields = new Component[]{status, orderId, firstName, lastName, phoneNumber, email, orderDescription,
                measurements, price, subtotal, total, date};

        formLayout.add(fields);
        editorDiv.add(avatarGroup, formLayout);
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
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(TrampolineOrder value) {
        this.trampolineOrder = value;
        String topic = null;
        if (this.trampolineOrder != null && this.trampolineOrder.getId() != null) {
            topic = "trampolineOrder/" + this.trampolineOrder.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.trampolineOrder);
        avatarGroup.setTopic(topic);

    }
}