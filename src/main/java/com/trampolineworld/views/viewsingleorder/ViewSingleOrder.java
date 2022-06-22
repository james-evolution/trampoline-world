package com.trampolineworld.views.viewsingleorder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.trampolineworld.data.Role;
import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.entity.User;
import com.trampolineworld.data.service.TrampolineOrderService;
import com.trampolineworld.data.service.UserRepository;
import com.trampolineworld.data.service.UserService;
import com.trampolineworld.views.*;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;

@Route(value = "view_order/:trampolineOrderID", layout = MainLayout.class)
@PageTitle("View Details")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed({ "ADMIN", "USER" })
public class ViewSingleOrder extends HorizontalLayout implements BeforeEnterObserver {

	private Button goBackButton = new Button("Go Back");

	private H2 orderIdTitle;
	private Paragraph firstNameParagraph, lastNameParagraph, phoneNumberParagraph, emailParagraph,
			orderDescriptionParagraph, measurementsParagraph, subtotalParagraph, totalParagraph, dateParagraph,
			completeParagraph;
	private Label firstNameLabel, lastNameLabel, phoneNumberLabel, emailLabel, descriptionLabel, measurementsLabel,
			subtotalLabel, totalLabel, dateLabel, completeLabel;
	private HorizontalLayout row1, row2, row3, row4, row5;

	private TrampolineOrder trampolineOrder;

	private final TrampolineOrderService trampolineOrderService;
	private final UserService userService;
	private final UserRepository userRepository;

	private final String TRAMPOLINEORDER_ID = "trampolineOrderID";

	public ViewSingleOrder(TrampolineOrderService trampolineOrderService, UserService userService,
			UserRepository userRepository) {
		this.trampolineOrderService = trampolineOrderService;
		this.userService = userService;
		this.userRepository = userRepository;
		addClassNames("single-order-view");
		setId("single-order-view");

		createHtmlElements();

		// Configure button appearance and click listener.
		goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		goBackButton.addClickListener(e -> {
			History history = UI.getCurrent().getPage().getHistory();
			history.back();
//			UI.getCurrent().navigate(TrampolineOrdersView.class); // Send user back to the Trampoline Orders page.
		});

		createLabels();
		createRows();

		// Create layout & add components to it.
		VerticalLayout layout = new VerticalLayout();
		layout.add(orderIdTitle);
		layout.add(row1);
		layout.add(row2);
		layout.add(descriptionLabel, orderDescriptionParagraph);
		layout.add(row3);
		layout.add(row4);
		layout.add(row5);
		layout.add(goBackButton);
		this.setClassName("view-single-order-layout");
		// Add layout to the view.
		this.add(layout);
	}

	private void createRows() {
		row1 = new HorizontalLayout();
		row1.setAlignItems(Alignment.BASELINE);
		row1.add(firstNameLabel, firstNameParagraph);
		row1.add(lastNameLabel, lastNameParagraph);

		row2 = new HorizontalLayout();
		row2.setAlignItems(Alignment.BASELINE);
		row2.add(phoneNumberLabel, phoneNumberParagraph);
		row2.add(emailLabel, emailParagraph);

		row3 = new HorizontalLayout();
		row3.setAlignItems(Alignment.BASELINE);
		row3.add(measurementsLabel, measurementsParagraph);

		row4 = new HorizontalLayout();
		row4.setAlignItems(Alignment.BASELINE);
		row4.add(subtotalLabel, subtotalParagraph);
		row4.add(totalLabel, totalParagraph);

		row5 = new HorizontalLayout();
		row5.setAlignItems(Alignment.BASELINE);
		row5.add(dateLabel, dateParagraph);
		row5.add(completeLabel, completeParagraph);
	}

	private void createLabels() {
		// Create labels.
		firstNameLabel = new Label("First Name:");
		firstNameLabel.addClassName("coloredLabel");

		lastNameLabel = new Label("Last Name:");
		lastNameLabel.addClassName("coloredLabel");

		phoneNumberLabel = new Label("Phone Number:");
		phoneNumberLabel.addClassName("coloredLabel");

		emailLabel = new Label("Email:");
		emailLabel.addClassName("coloredLabel");

		descriptionLabel = new Label("Order Description:");
		descriptionLabel.addClassName("coloredLabel");

		measurementsLabel = new Label("Measurements:");
		measurementsLabel.addClassName("coloredLabel");

		subtotalLabel = new Label("Subtotal:");
		subtotalLabel.addClassName("coloredLabel");

		totalLabel = new Label("Total:");
		totalLabel.addClassName("coloredLabel");

		dateLabel = new Label("Date:");
		dateLabel.addClassName("coloredLabel");

		completeLabel = new Label("Status:");
		completeLabel.addClassName("coloredLabel");
	}

	private void createHtmlElements() {
		// Create HTML elements.
		orderIdTitle = new H2();
		firstNameParagraph = new Paragraph();
		lastNameParagraph = new Paragraph();
		phoneNumberParagraph = new Paragraph();
		emailParagraph = new Paragraph();
		orderDescriptionParagraph = new Paragraph();
		measurementsParagraph = new Paragraph();
		subtotalParagraph = new Paragraph();
		totalParagraph = new Paragraph();
		dateParagraph = new Paragraph();
		completeParagraph = new Paragraph();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		// Fetch order id from route URL parameters.
		Optional<Long> trampolineOrderId = event.getRouteParameters().get(TRAMPOLINEORDER_ID).map(Long::valueOf);

		if (trampolineOrderId.isPresent()) {
			// Get the TrampolineOrder object from the database.
			Optional<TrampolineOrder> trampolineOrderFromBackend = trampolineOrderService.get(trampolineOrderId.get());
			if (trampolineOrderFromBackend.isPresent()) {
				// Load order data into the UI components.
				loadComponentData(trampolineOrderFromBackend.get());
			}
		}
	}

	private void loadComponentData(TrampolineOrder value) {
		this.trampolineOrder = value;

		// If an order & id is present, load data into the ui components.
		if (this.trampolineOrder != null && this.trampolineOrder.getId() != null) {
			orderIdTitle.add("Order " + this.trampolineOrder.getId().toString());
			firstNameParagraph.setText(this.trampolineOrder.getFirstName());
			lastNameParagraph.setText(this.trampolineOrder.getLastName());
			phoneNumberParagraph.setText(this.trampolineOrder.getPhoneNumber());
			emailParagraph.setText(this.trampolineOrder.getEmail());
			orderDescriptionParagraph.setText(this.trampolineOrder.getOrderDescription());
			measurementsParagraph.setText(this.trampolineOrder.getMeasurements());
			subtotalParagraph.setText(String.valueOf(this.trampolineOrder.getSubtotal()));
			totalParagraph.setText(String.valueOf(this.trampolineOrder.getTotal()));
			dateParagraph.setText(this.trampolineOrder.getDate().toString());
			String complete = "";
			if (this.trampolineOrder.isComplete()) {
				complete = "COMPLETE";
				completeParagraph.removeClassName("incompleteText");
				completeParagraph.addClassName("completeText");
			} else {
				complete = "INCOMPLETE";
				completeParagraph.removeClassName("completeText");
				completeParagraph.addClassName("incompleteText");
			}
			completeParagraph.setText(complete);
		}
	}
}