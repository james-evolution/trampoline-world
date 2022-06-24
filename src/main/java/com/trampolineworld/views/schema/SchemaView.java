package com.trampolineworld.views.schema;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import javax.annotation.security.RolesAllowed;
import com.trampolineworld.views.*;

@Route(value = "schema", layout = MainLayout.class)
@PageTitle("Database Structure")
//@CssImport("./styles/views/view_order/single-order-view.css")
//@RouteAlias(value = "", layout = MainLayout.class)
@CssImport(themeFor = "vaadin-horizontal-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-vertical-layout", value = "./themes/trampolineworld/views/userguide-theme.css")
@CssImport(themeFor = "vaadin-tabs", value = "./themes/trampolineworld/views/userguide-theme.css")
@RolesAllowed({ "ADMIN", "TECH" })
public class SchemaView extends HorizontalLayout {

	private IFrame schemaFrame;
	private Paragraph captionParagraph;
	private boolean fullScreen = true;
	private H2 title = new H2("What is this for?");
	private Button buttonToggleDescription = new Button("Toggle Description");
	
	SplitLayout splitLayout;
	HorizontalLayout buttonRow;
	VerticalLayout captionVerticalLayout;
	VerticalLayout secondaryVerticalLayout;
	
	public SchemaView() {
		// Configure parent layout.
		addClassNames("userguide-view");
		setId("userguide-view");
		setClassName("userguide-layout");
		setWidthFull();
		getStyle().set("opacity", "0.8 !important");
		setSizeFull();

		loadSchemaFrame();
		loadCaptionParagraph();
		
		// Create layouts.
		splitLayout = new SplitLayout();
		splitLayout.setSplitterPosition(36);
		splitLayout.setWidth("100%");
		splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		secondaryVerticalLayout = new VerticalLayout();
		buttonRow = new HorizontalLayout();
		// Create button & add to row.
		configureButtonToggleDescription(splitLayout);
		buttonRow.add(buttonToggleDescription);
		// Style title.
		title.getStyle().set("margin-top", "14px !important");
		// Add title & caption paragraph to vertical layout, make hidden by default.
		captionVerticalLayout = new VerticalLayout(title, captionParagraph);
		captionVerticalLayout.setVisible(false);
		
		// Add primary & secondary components to splitLayout.	
		splitLayout.addToPrimary(captionVerticalLayout);
//		splitLayout.addToSecondary(new VerticalLayout(buttonRow, schemaFrame));
		secondaryVerticalLayout.add(buttonRow, schemaFrame);
		splitLayout.addToSecondary(secondaryVerticalLayout);
		
		// Add splitLayout to parent layout.
		add(splitLayout);
	}

	private void configureButtonToggleDescription(SplitLayout splitLayout) {
		buttonToggleDescription.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonToggleDescription.addClickListener(e -> {
			// If already full screen, turn it off & show instructions.
			if (fullScreen) {
				fullScreen = false;
				secondaryVerticalLayout.remove(buttonRow);
				captionVerticalLayout.removeAll();
				captionVerticalLayout.add(buttonRow, title, captionParagraph);
				splitLayout.getPrimaryComponent().setVisible(true);
			}
			// If not full screen, make it full screen and hide instructions.
			else {
				fullScreen = true;
				splitLayout.getPrimaryComponent().setVisible(false);
				secondaryVerticalLayout.removeAll();
				secondaryVerticalLayout.add(buttonRow, schemaFrame);
			}
		});
	}

	private void loadCaptionParagraph() {
		captionParagraph = new Paragraph(
				"In the event that you ever wish to use your own database for storing this system's data (and for whatever reason, I'm not around to help) you'll first have to create one."
						+ " It's critical that the database schema (design/structure) is the same as the current one. This means you'll need to re-create the same tables that this system currently relies on, with the same columns, constraints, and primary keys."
						+ " That's what this page is here to help you with. It outlines the precise structure of the system's current database so that you can create your own identical copy. Once you've created a database with these five tables and their matching structures, you're ready to link it to the application."
						+ " At the moment the path to this system's current database is hardcoded in a file named 'application.properties' A user would have to change the values of the variables titled spring.datasource.url, spring.datasource.username, and spring.datasource.password to point to a new database."
						+ " Finally, they'd have to rebuild the project and redeploy it to the host. An in-depth video tutorial will be made for this in the future to guide you through the process. In the meantime, though, so long as I'm available, I'd be happy to help you do this myself.");
		captionParagraph.getStyle().set("margin-left", "20px");
	}

	private void loadSchemaFrame() {
		schemaFrame = new IFrame();
		schemaFrame.setSrc("https://faintdev.net/trampolineworld/schema.html");
		schemaFrame.getStyle().set("width", "100% !important");
		schemaFrame.getStyle().set("height", "100% !important");
		schemaFrame.getStyle().set("box-shadow", "none !important");
		schemaFrame.getStyle().set("border-style", "none !important");
	}
}