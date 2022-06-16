package com.trampolineworld.views.export;

import javax.annotation.security.RolesAllowed;

import org.vaadin.reports.PrintPreviewReport;

import com.trampolineworld.data.entity.TrampolineOrder;
import com.trampolineworld.data.service.TrampolineOrderService;
import com.trampolineworld.views.MainLayout;
import com.trampolineworld.views.trampolineorders.TrampolineOrdersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@Route(value = "export", layout = MainLayout.class)
@PageTitle("Export Data")
@RolesAllowed("ADMIN")
@CssImport(
	    themeFor = "vaadin-vertical-layout vaadin-horizontal-layout",
	    value = "./themes/trampolineworld/views/export-theme.css"
	)
public class ExportView extends VerticalLayout {
	
	private Button goBackButton = new Button("Home");
	
	public ExportView(TrampolineOrderService trampolineOrderService) {
        
		// Configure button appearance and click listener.
        goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goBackButton.addClickListener(e -> {
        	UI.getCurrent().navigate(TrampolineOrdersView.class); // Send user back to the Trampoline Orders page.
        });
        
		PrintPreviewReport report = new PrintPreviewReport<>(TrampolineOrder.class, "complete", "firstName", "lastName", "phoneNumber", "email", "orderDescription", "measurements", "subtotal", "total", "date");
		report.setItems(trampolineOrderService.findAllNoFilter());
		StreamResource pdf = report.getStreamResource("trampoline_orders.pdf", trampolineOrderService::findAllNoFilter, PrintPreviewReport.Format.PDF);
		StreamResource csv = report.getStreamResource("trampoline_orders.csv", trampolineOrderService::findAllNoFilter, PrintPreviewReport.Format.CSV);
		
//		HorizontalLayout row1 = new HorizontalLayout(goBackButton);
//		row1.addClassName("centeredRow");
		
		HorizontalLayout row1 = new HorizontalLayout(new Anchor(pdf, "Export PDF"), new Anchor(csv, "Export CSV"));
		row1.addClassName("centeredRow");
		add(
			row1,
			report
		);
		
		setWidthFull();
	}
}
